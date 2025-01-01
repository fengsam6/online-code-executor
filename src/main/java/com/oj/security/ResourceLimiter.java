package com.oj.security;

public class ResourceLimiter {
    public static void setMemoryLimit(int maxMemoryMB) {
        long maxMemoryBytes = maxMemoryMB * 1024L * 1024L;
        Runtime.getRuntime().maxMemory();
        
        // 设置堆内存限制
        if (Runtime.getRuntime().maxMemory() > maxMemoryBytes) {
            throw new SecurityException("Requested memory exceeds limit");
        }
    }
} 