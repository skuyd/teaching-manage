package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teaching.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_group")
public class Group extends BaseEntity {

    private Long subjectId;

    private String name;

    private Long leaderId;  // 组长用户ID
}
