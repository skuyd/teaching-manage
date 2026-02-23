package com.teaching.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserImportResultDTO {
    private int totalCount;
    private int successCount;
    private int failCount;
    private List<String> errors = new ArrayList<>();

    public void addError(String error) {
        this.errors.add(error);
        this.failCount++;
    }

    public void incrementSuccess() {
        this.successCount++;
    }
}
