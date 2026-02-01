package com.teaching.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubmissionTest {

    @Test
    @DisplayName("创建Submission实体成功")
    void createSubmission_shouldSuccess() {
        Submission submission = new Submission();
        submission.setId(1L);
        submission.setLessonId(100L);
        submission.setSubmitterId(10L);
        submission.setGroupId(5L);
        submission.setFilePath("/uploads/1/100/5/code.zip");
        submission.setSubmitTime(LocalDateTime.now());

        assertEquals(1L, submission.getId());
        assertEquals(100L, submission.getLessonId());
        assertEquals(10L, submission.getSubmitterId());
        assertEquals(5L, submission.getGroupId());
        assertEquals("/uploads/1/100/5/code.zip", submission.getFilePath());
        assertNotNull(submission.getSubmitTime());
    }

    @Test
    @DisplayName("个人作业提交（无小组ID）")
    void createPersonalSubmission_shouldSuccess() {
        Submission submission = new Submission();
        submission.setLessonId(100L);
        submission.setSubmitterId(10L);
        submission.setGroupId(null);  // 个人作业没有小组ID
        submission.setFilePath("/uploads/1/100/10/code.zip");
        submission.setSubmitTime(LocalDateTime.now());

        assertNull(submission.getGroupId());
        assertNotNull(submission.getSubmitterId());
    }
}
