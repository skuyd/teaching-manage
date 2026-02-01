package com.teaching.dto;

import com.teaching.entity.Subject;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateSubjectRequest {

    private String name;

    private String description;

    private Boolean isGrouped;

    @Min(value = 1, message = "最小人数不能小于1")
    private Integer minMembers;

    @Min(value = 1, message = "最大人数不能小于1")
    private Integer maxMembers;

    private LocalDate startDate;

    private LocalDate endDate;

    public void updateEntity(Subject subject) {
        if (this.name != null) {
            subject.setName(this.name);
        }
        if (this.description != null) {
            subject.setDescription(this.description);
        }
        if (this.isGrouped != null) {
            subject.setIsGrouped(this.isGrouped);
        }
        if (this.minMembers != null) {
            subject.setMinMembers(this.minMembers);
        }
        if (this.maxMembers != null) {
            subject.setMaxMembers(this.maxMembers);
        }
        if (this.startDate != null) {
            subject.setStartDate(this.startDate);
        }
        if (this.endDate != null) {
            subject.setEndDate(this.endDate);
        }
    }
}
