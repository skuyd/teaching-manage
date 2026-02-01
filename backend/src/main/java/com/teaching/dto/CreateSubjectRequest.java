package com.teaching.dto;

import com.teaching.entity.Subject;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateSubjectRequest {

    @NotBlank(message = "学科名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "是否分组不能为空")
    private Boolean isGrouped = false;

    @Min(value = 1, message = "最小人数不能小于1")
    private Integer minMembers = 1;

    @Min(value = 1, message = "最大人数不能小于1")
    private Integer maxMembers = 1;

    private LocalDate startDate;

    private LocalDate endDate;

    public Subject toEntity() {
        Subject subject = new Subject();
        subject.setName(this.name);
        subject.setDescription(this.description != null ? this.description : "");
        subject.setIsGrouped(this.isGrouped);
        subject.setMinMembers(this.minMembers);
        subject.setMaxMembers(this.maxMembers);
        subject.setStartDate(this.startDate);
        subject.setEndDate(this.endDate);
        return subject;
    }
}
