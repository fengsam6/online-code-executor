package com.oj.security.controller;

import com.oj.security.executor.*;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin
public class CodeExecuteController {

    private final JavaExecutor javaExecutor;
    private final CppExecutor cppExecutor;
    private final CExecutor cExecutor;
    private final PythonExecutor pythonExecutor;
    private final GoExecutor goExecutor;

    @PostMapping("/execute")
    public ResponseEntity<ExecutionResult> executeCode(@RequestBody ExecuteRequest request) {
        LanguageExecutor executor = getExecutor(request.getLanguage());
        ExecutionResult result = executor.execute(request.getCode());
        result.setInput(request.getInput());  // 设置输入参数
        return ResponseEntity.ok(result);
    }

    private LanguageExecutor getExecutor(String language) {
        return switch (language.toLowerCase()) {
            case "java" -> javaExecutor;
            case "cpp" -> cppExecutor;
            case "c" -> cExecutor;
            case "python" -> pythonExecutor;
            case "go" -> goExecutor;
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }

    @Data
    public static class ExecuteRequest {
        private String language;
        private String code;
        private String input;
    }
} 