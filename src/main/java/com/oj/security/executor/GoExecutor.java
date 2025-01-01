package com.oj.security.executor;

import org.springframework.stereotype.Component;

/**
 * Go代码执行器
 * 处理Go代码的编译和运行，支持并发安全
 */
@Component
public class GoExecutor extends DockerExecutor {
    
    @Override
    protected String getFileExtension() {
        return ".go";
    }
    
    @Override
    public ExecutionResult execute(String code) {
        return runInDocker("go run -gcflags='-N -l'", code);
    }
} 