package com.teaching.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateGroupRequest {

    @NotNull(message = "学科ID不能为空")
    private Long subjectId;

    @NotBlank(message = "小组名称不能为空")
    private String name;
}
