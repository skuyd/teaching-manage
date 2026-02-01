package com.teaching.dto;

import lombok.Data;

@Data
public class PageRequest {

    private Integer page = 1;
    private Integer size = 10;
    private String keyword;

    public int getOffset() {
        return (page - 1) * size;
    }
}
