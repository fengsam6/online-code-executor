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
    
    public static ExecutionResult newFailRes(String error) {
        return new ExecutionResult(false, null, error);
    }
} 