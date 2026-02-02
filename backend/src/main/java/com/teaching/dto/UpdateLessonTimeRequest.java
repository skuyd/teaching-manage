package com.teaching.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 调整课程时间请求
 */
@Data
public class UpdateLessonTimeRequest {

    @NotNull(message = "上课时间不能为空")
    private LocalDateTime lessonTime;
}
