package com.oj.security.executor;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.Semaphore;

@Component
public class JavaExecutor extends DockerExecutor {
    
    private static final Pattern CLASS_NAME_PATTERN = 
        Pattern.compile("public\\s+class\\s+(\\w+)");
    
    private static final int MAX_CODE_LENGTH = 10000;
    
    private final Semaphore executionSemaphore = new Semaphore(1);
    
    @Override
    protected String getFileExtension() {
        return ".java";
    }
    
    @Override
    protected String getFileName(String code) {
        Matcher matcher = CLASS_NAME_PATTERN.matcher(code);
        if (matcher.find()) {
            String className = matcher.group(1);
            if (className.matches("[A-Za-z_$][A-Za-z0-9_$]*")) {
                return className;
            }
        }
        return "Solution";
    }
    
    @Override
    public ExecutionResult execute(String code) {
        return execute(code, "");
    }
    
    @Override
    public ExecutionResult execute(String code, String input) {
        // 检查package声明
        if (code.contains("package ")) {
            return new ExecutionResult(false, null, "不支持package声明");
        }
        
        // 验证代码长度
        if (code == null || code.length() > MAX_CODE_LENGTH) {
            return new ExecutionResult(false, null, "代码长度超过限制");
        }
        
        try {
            executionSemaphore.acquire();
            try {
                return runInDocker("javac-and-run", code, input);
            } finally {
                executionSemaphore.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ExecutionResult(false, null, "执行被中断");
        }
    }
} 