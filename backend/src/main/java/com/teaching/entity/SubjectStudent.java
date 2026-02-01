package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teaching.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_subject_student")
public class SubjectStudent extends BaseEntity {

    private Long subjectId;

    private Long studentId;
}
