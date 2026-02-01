package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teaching.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_subject")
public class Subject extends BaseEntity {

    private String name;

    private String description;

    private Boolean isGrouped = false;

    private Integer minMembers = 1;

    private Integer maxMembers = 1;

    private LocalDate startDate;

    private LocalDate endDate;
}
