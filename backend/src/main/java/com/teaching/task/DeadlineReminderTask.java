package com.teaching.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teaching.entity.Lesson;
import com.teaching.entity.NotificationType;
import com.teaching.entity.Submission;
import com.teaching.entity.SubjectStudent;
import com.teaching.mapper.LessonMapper;
import com.teaching.mapper.SubmissionMapper;
import com.teaching.mapper.SubjectStudentMapper;
import com.teaching.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 截止日期提醒定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineReminderTask {

    private final LessonMapper lessonMapper;
    private final SubmissionMapper submissionMapper;
    private final SubjectStudentMapper subjectStudentMapper;
    private final NotificationService notificationService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 每天早上9点执行，提醒截止时间在明天的作业
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendDeadlineReminders() {
        log.info("开始执行截止日期提醒任务");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrowStart = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime tomorrowEnd = tomorrowStart.plusDays(1);

        // 查询截止时间在明天的课程
        LambdaQueryWrapper<Lesson> lessonWrapper = new LambdaQueryWrapper<>();
        lessonWrapper.isNotNull(Lesson::getDeadline)
                .ge(Lesson::getDeadline, tomorrowStart)
                .lt(Lesson::getDeadline, tomorrowEnd);

        List<Lesson> lessons = lessonMapper.selectList(lessonWrapper);

        if (lessons.isEmpty()) {
            log.info("没有即将截止的作业");
            return;
        }

        log.info("找到 {} 个即将截止的作业", lessons.size());

        for (Lesson lesson : lessons) {
            sendReminderForLesson(lesson);
        }

        log.info("截止日期提醒任务执行完成");
    }

    /**
     * 为单个课程发送提醒
     */
    private void sendReminderForLesson(Lesson lesson) {
        // 获取该学科的所有学员
        List<Long> allStudentIds = subjectStudentMapper.selectStudentIdsBySubjectId(lesson.getSubjectId());

        if (allStudentIds.isEmpty()) {
            return;
        }

        // 查询已提交的学员
        LambdaQueryWrapper<Submission> submissionWrapper = new LambdaQueryWrapper<>();
        submissionWrapper.eq(Submission::getLessonId, lesson.getId());
        List<Submission> submissions = submissionMapper.selectList(submissionWrapper);

        // 收集已提交的学员ID
        Set<Long> submittedStudentIds = new HashSet<>();
        for (Submission submission : submissions) {
            if (submission.getGroupId() != null) {
                // 小组提交，查询所有组员
                // 这里简化处理，直接用提交者ID
                submittedStudentIds.add(submission.getSubmitterId());
            } else {
                // 个人提交
                submittedStudentIds.add(submission.getSubmitterId());
            }
        }

        // 筛选出未提交的学员
        Set<Long> unsubmittedStudentIds = new HashSet<>(allStudentIds);
        unsubmittedStudentIds.removeAll(submittedStudentIds);

        if (unsubmittedStudentIds.isEmpty()) {
            log.info("课程 {} 所有学员已提交，无需提醒", lesson.getTitle());
            return;
        }

        // 发送提醒通知
        String title = "作业截止提醒";
        String content = String.format("课程《%s》的作业即将于 %s 截止，请尽快提交！",
                lesson.getTitle(),
                lesson.getDeadline().format(DATE_FORMATTER));

        notificationService.createNotifications(
                unsubmittedStudentIds.stream().toList(),
                title,
                content,
                NotificationType.DEADLINE_REMINDER
        );

        log.info("已为课程 {} 发送 {} 条截止提醒", lesson.getTitle(), unsubmittedStudentIds.size());
    }
}
