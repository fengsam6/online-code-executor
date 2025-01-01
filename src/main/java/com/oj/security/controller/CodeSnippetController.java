package com.oj.security.controller;

import com.oj.security.entity.CodeSnippet;
import com.oj.security.service.CodeSnippetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/snippets")
@RequiredArgsConstructor
@CrossOrigin
public class CodeSnippetController {
    
    private final CodeSnippetService codeSnippetService;
    
    @PostMapping
    public ResponseEntity<CodeSnippet> saveSnippet(@RequestBody CodeSnippet snippet) {
        try {
            return ResponseEntity.ok(codeSnippetService.saveSnippet(snippet));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "保存失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CodeSnippet> getSnippet(@PathVariable String id) {
        try {
            return ResponseEntity.ok(codeSnippetService.getSnippet(id));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "代码片段不存在");
        }
    }
    
    @GetMapping
    public ResponseEntity<List<CodeSnippet>> listSnippets() {
        try {
            return ResponseEntity.ok(codeSnippetService.listSnippets());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "获取列表失败");
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSnippet(@PathVariable String id) {
        try {
            codeSnippetService.deleteSnippet(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "删除失败: " + e.getMessage());
        }
    }
} 