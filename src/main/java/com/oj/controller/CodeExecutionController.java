package com.oj.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.oj.security.executor.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/code")
public class CodeExecutionController {
    
    private final Map<String, LanguageExecutor> executors;
    
    @Autowired
    public CodeExecutionController(
            JavaExecutor javaExecutor,
            CppExecutor cppExecutor,
            CExecutor cExecutor,
            GoExecutor goExecutor,
            PythonExecutor pythonExecutor) {
        
        executors = new ConcurrentHashMap<>();
        executors.put("java", javaExecutor);
        executors.put("cpp", cppExecutor);
        executors.put("c", cExecutor);
        executors.put("go", goExecutor);
        executors.put("python", pythonExecutor);
    }
    
    @PostMapping("/run")
    public ExecutionResult runCode(@RequestBody CodeRequest request) {
        LanguageExecutor executor = executors.get(request.getLanguage());
        if (executor == null) {
            return ExecutionResult.newFailRes(
                "不支持的编程语言: " + request.getLanguage()
            );
        }
        
       return executor.execute(request.getCode());
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class CodeRequest {
    private String language;
    private String code;
} 