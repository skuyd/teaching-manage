package com.teaching.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinGroupRequest {

    @NotNull(message = "小组ID不能为空")
    private Long groupId;
}
