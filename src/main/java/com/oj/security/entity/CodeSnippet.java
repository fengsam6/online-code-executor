package com.oj.security.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeSnippet {
    private String id;
    private String language;
    private String code;
    private String input;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String title;
    private String description;
} 