package com.oj.security.executor;

import org.springframework.stereotype.Component;

@Component
public class PythonExecutor extends DockerExecutor {
    
    @Override
    protected String getFileExtension() {
        return ".py";
    }
    
    @Override
    public ExecutionResult execute(String code) {
        return execute(code, "");
    }
    
    @Override
    public ExecutionResult execute(String code, String input) {
        return runInDocker("python3", code, input);
    }
} 