package com.oj.security.executor;

public interface LanguageExecutor {
    ExecutionResult execute(String code);
    ExecutionResult execute(String code, String input);
} 