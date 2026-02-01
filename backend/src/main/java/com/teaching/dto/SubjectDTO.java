package com.teaching.dto;

import com.teaching.entity.Subject;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SubjectDTO {

    private Long id;
    private String name;
    private String description;
    private Boolean isGrouped;
    private Integer minMembers;
    private Integer maxMembers;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createTime;

    // 额外字段
    private Integer studentCount;  // 学员数量
    private Integer lessonCount;   // 课程数量

    public static SubjectDTO fromEntity(Subject subject) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        dto.setDescription(subject.getDescription());
        dto.setIsGrouped(subject.getIsGrouped());
        dto.setMinMembers(subject.getMinMembers());
        dto.setMaxMembers(subject.getMaxMembers());
        dto.setStartDate(subject.getStartDate());
        dto.setEndDate(subject.getEndDate());
        dto.setCreateTime(subject.getCreateTime());
        return dto;
    }
}
