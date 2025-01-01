package com.oj.security.service.impl;

import com.oj.security.entity.CodeSnippet;
import com.oj.security.service.CodeSnippetService;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryCodeSnippetService implements CodeSnippetService {
    
    private final Map<String, CodeSnippet> snippets = new ConcurrentHashMap<>();
    
    @Override
    public CodeSnippet saveSnippet(CodeSnippet snippet) {
        // 生成唯一ID
        if (snippet.getId() == null) {
            snippet.setId(UUID.randomUUID().toString());
        }
        
        // 设置时间
        LocalDateTime now = LocalDateTime.now();
        if (snippet.getCreateTime() == null) {
            snippet.setCreateTime(now);
        }
        snippet.setUpdateTime(now);
        
        // 保存代码片段
        snippets.put(snippet.getId(), snippet);
        return snippet;
    }
    
    @Override
    public CodeSnippet getSnippet(String id) {
        CodeSnippet snippet = snippets.get(id);
        if (snippet == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "代码片段不存在");
        }
        return snippet;
    }
    
    @Override
    public List<CodeSnippet> listSnippets() {
        return snippets.values().stream()
            .sorted(Comparator.comparing(CodeSnippet::getUpdateTime).reversed())
            .toList();
    }
    
    @Override
    public void deleteSnippet(String id) {
        if (snippets.remove(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "代码片段不存在");
        }
    }
} 