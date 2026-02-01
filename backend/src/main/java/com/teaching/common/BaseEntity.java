package com.teaching.common;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Version
    private Integer version = 0;

    @TableLogic
    private Integer delFlag = 0;

    @TableField(fill = FieldFill.INSERT)
    private String createBy = "";

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy = "";

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
