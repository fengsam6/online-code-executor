package com.oj.security.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * C代码执行器
 * 处理C代码的编译和运行，支持跨平台
 */
@Slf4j
@Component
public class CExecutor extends DockerExecutor {
    
    @Override
    protected String getFileExtension() {
        return ".c";
    }
    
    @Override
    public ExecutionResult execute(String code) {
        return execute(code, "");
    }
    
    @Override
    public ExecutionResult execute(String code, String input) {
        String compileCommand = "gcc -O2 -Wall -fno-asm -D_FORTIFY_SOURCE=2 -o Main";
        return runInDocker(compileCommand, code, input);
    }
} 