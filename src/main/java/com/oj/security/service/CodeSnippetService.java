package com.oj.security.service;

import com.oj.security.entity.CodeSnippet;
import java.util.List;

public interface CodeSnippetService {
    CodeSnippet saveSnippet(CodeSnippet snippet);
    CodeSnippet getSnippet(String id);
    List<CodeSnippet> listSnippets();
    void deleteSnippet(String id);
} 