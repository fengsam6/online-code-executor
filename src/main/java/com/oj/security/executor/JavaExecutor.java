package com.oj.security.executor;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JavaExecutor extends DockerExecutor {
    
    private static final Pattern CLASS_NAME_PATTERN = 
        Pattern.compile("public\\s+class\\s+(\\w+)");
    
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
        if (code.contains("package ")) {
            return new ExecutionResult(false, null, "不支持package声明");
        }
        return runInDocker("javac-and-run", code);
    }
} 