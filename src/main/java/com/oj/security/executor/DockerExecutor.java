package com.oj.security.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.concurrent.*;
import java.nio.file.*;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Docker容器中代码执行的基类
 * 使用虚拟线程和信号量控制并发
 */
@Slf4j
@Component
public abstract class DockerExecutor implements LanguageExecutor {
    
    @Value("${executor.scripts-dir}")
    private String scriptsDir;
    
    @Value("${executor.temp-dir}")
    private String tempDir;
    
    @Value("${executor.timeout:5}")
    private int executionTimeout;
    
    @Value("${executor.max-concurrent:10}")
    private int maxConcurrent;
    
    private static final int OUTPUT_LIMIT = 1000; // 输出行数限制
    private static final int MAX_CODE_LENGTH = 65536; // 代码长度限制64KB
    
    private final Semaphore executionSemaphore;
    private final ExecutorService executor;
    private final ThreadPoolExecutor ioExecutor;
    private final int BATCH_SIZE;
    private final BlockingQueue<CompletableFuture<ExecutionResult>> batchQueue;
    
    // 添加结果缓存
    private static final Cache<String, ExecutionResult> resultCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build();
    
    public DockerExecutor() {
        this.executionSemaphore = new Semaphore(maxConcurrent);
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.ioExecutor = new ThreadPoolExecutor(
            4, 8, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        this.BATCH_SIZE = 10;
        this.batchQueue = new LinkedBlockingQueue<>(BATCH_SIZE);
    }

    /**
     * 在Docker容器中执行代码
     */
    protected ExecutionResult runInDocker(String command, String code) {
        // 检查缓存
        String cacheKey = command + ":" + code.hashCode();
        ExecutionResult cachedResult = resultCache.getIfPresent(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        try {
            CompletableFuture<ExecutionResult> future = CompletableFuture
                .supplyAsync(() -> executeInDocker(command, code), executor)
                .thenApplyAsync(result -> {
                    // 异步处理IO操作
                    cleanupResources();
                    return result;
                }, ioExecutor);

            // 添加到批处理队列
            if (batchQueue.offer(future)) {
                processBatchIfNeeded();
            }

            ExecutionResult result = future.get(executionTimeout + 1, TimeUnit.SECONDS);
            // 缓存结果
            resultCache.put(cacheKey, result);
            return result;

        } catch (Exception e) {
            log.error("执行失败", e);
            return new ExecutionResult(false, null, "执行错误: " + e.getMessage());
        }
    }

    private ExecutionResult executeInDocker(String command, String code) {
        Process process = null;
        Path codeFile = null;
        
        try {
            // 获取脚本路径
            String runCmd = getScriptPath();
            
            // 创建临时文件
            codeFile = createCodeFile(code);
            
            // 构建进程
            ProcessBuilder pb = new ProcessBuilder(
                runCmd,
                command,
                codeFile.toString()
            );

            // 设置环境变量
            pb.environment().put("PATH", "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");
            pb.redirectErrorStream(true);

            // 启动进程
            process = pb.start();

            // 读取输出
            String output = readProcessOutput(process);
            
            // 等待完成
            boolean finished = process.waitFor(executionTimeout, TimeUnit.SECONDS);
            if (!finished) {
                return new ExecutionResult(false, null, "执行超时");
            }

            // 检查退出码
            int exitCode = process.exitValue();
            return exitCode == 0
                ? new ExecutionResult(true, output, null)
                : new ExecutionResult(false, null, "执行失败: " + output);

        } catch (Exception e) {
            log.error("执行代码失败", e);
            return new ExecutionResult(false, null, "执行错误: " + e.getMessage());
        } finally {
            // 清理资源
            if (process != null) {
                process.destroyForcibly();
            }
            if (codeFile != null) {
                try {
                    Files.deleteIfExists(codeFile);
                } catch (IOException e) {
                    // 忽略删除失败
                }
            }
        }
    }

    private String getScriptPath() {
        boolean isRunningFromJar = Thread.currentThread().getContextClassLoader()
                .getResource("org/springframework/boot/loader/Launcher.class") != null;
                //todo 带完善路径
        return  scriptsDir + "/run-code.sh";
//        return isRunningFromJar
//            ? scriptsDir + "/run-code.sh"
//            : getClass().getClassLoader().getResource("scripts/run-code.sh").getPath();
    }

    private String readProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                .limit(OUTPUT_LIMIT)
                .collect(Collectors.joining("\n"));
        }
    }

    private Path createCodeFile(String code) throws IOException {
        Path tempPath = Paths.get(tempDir);
        if (!Files.exists(tempPath)) {
            Files.createDirectories(tempPath);
        }
        
        String fileName = getFileName(code);
        Path codeFile = tempPath.resolve(fileName + getFileExtension());
        Files.writeString(codeFile, code, StandardCharsets.UTF_8);
        return codeFile;
    }

    protected String getFileName(String code) {
        return "Main";
    }

    protected abstract String getFileExtension();

    @PreDestroy
    public void cleanup() {
        // 关闭执行器
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 清理临时目录
        try {
            Path tempPath = Paths.get(tempDir);
            if (Files.exists(tempPath)) {
                Files.walk(tempPath)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // 忽略删除失败
                        }
                    });
            }
        } catch (IOException e) {
            // 忽略清理失败
        }
    }

    // 批量处理逻辑
    private void processBatchIfNeeded() {
        if (batchQueue.size() >= BATCH_SIZE) {
            List<CompletableFuture<ExecutionResult>> batch = new ArrayList<>();
            batchQueue.drainTo(batch, BATCH_SIZE);
            
            CompletableFuture.allOf(batch.toArray(new CompletableFuture[0]))
                .thenRunAsync(this::cleanupBatchResources, ioExecutor);
        }
    }

    // 资源清理优化
    private void cleanupResources() {
        try {
            Files.walk(Paths.get(tempDir))
                .filter(path -> Files.isRegularFile(path))
                .filter(this::isExpiredFile)
                .forEach(this::deleteFile);
        } catch (IOException e) {
            log.error("清理资源失败", e);
        }
    }

    private boolean isExpiredFile(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant()
                .isBefore(Instant.now().minus(5, ChronoUnit.MINUTES));
        } catch (IOException e) {
            return false;
        }
    }

    private void deleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("删除文件失败: {}", path, e);
        }
    }
} 