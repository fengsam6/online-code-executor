package com.oj.security.executor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecutionResult {
    private boolean success;
    private String output;
    private String error;
    private String input;
    
    public ExecutionResult(boolean success, String output, String error) {
        this(success, output, error, null);
    }
    
    public static ExecutionResult newFailRes(String error) {
        return new ExecutionResult(false, null, error, null);
    }
    
    public static ExecutionResult newFailRes(String error, String input) {
        return new ExecutionResult(false, null, error, input);
    }
    
    public static ExecutionResult newSuccessRes(String output, String input) {
        return new ExecutionResult(true, output, null, input);
    }
} 