package com.oj.security.executor;

import org.springframework.stereotype.Component;

/**
 * C++代码执行器
 * 处理C++代码的编译和运行
 */
@Component
public class CppExecutor extends DockerExecutor {
    
    @Override
    protected String getFileExtension() {
        return ".cpp";
    }
    
    @Override
    public ExecutionResult execute(String code) {
        return runInDocker("g++ -O2 -Wall -std=c++17 -fno-asm -D_FORTIFY_SOURCE=2", code);
    }
} 