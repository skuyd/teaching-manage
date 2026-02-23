package com.teaching.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.dto.CreateGradeRequest;
import com.teaching.dto.GradeDTO;
import com.teaching.dto.UpdateGradeRequest;
import com.teaching.entity.*;
import com.teaching.enums.GradeLevel;
import com.teaching.enums.UserRole;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.GradeMapper;
import com.teaching.mapper.GroupMemberMapper;
import com.teaching.mapper.LessonMapper;
import com.teaching.mapper.SubmissionMapper;
import com.teaching.mapper.UserMapper;
import com.teaching.service.GradeService;
import com.teaching.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradeServiceImpl extends ServiceImpl<GradeMapper, Grade> implements GradeService {

    private final GradeMapper gradeMapper;
    private final SubmissionMapper submissionMapper;
    private final LessonMapper lessonMapper;
    private final UserMapper userMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public GradeDTO gradeSubmission(CreateGradeRequest request, Long graderId) {
        // 验证提交是否存在
        Submission submission = submissionMapper.selectById(request.getSubmissionId());
        if (submission == null) {
            throw new BusinessException(404, "提交记录不存在");
        }

        // 检查是否已经评分
        Grade existing = gradeMapper.findBySubmissionId(request.getSubmissionId());
        if (existing != null) {
            throw new BusinessException(400, "该作业已评分，请使用更新功能");
        }

        // 创建评分
        Grade grade = new Grade();
        grade.setSubmissionId(request.getSubmissionId());
        grade.setGrade(request.getGrade());
        grade.setComment(request.getComment());
        grade.setGraderId(graderId);
        grade.setGradeTime(LocalDateTime.now());

        gradeMapper.insert(grade);
        log.info("评分已添加: id={}, submissionId={}, grade={}, graderId={}",
                grade.getId(), request.getSubmissionId(), request.getGrade(), graderId);

        // 发送评分完成通知
        Lesson lesson = lessonMapper.selectById(submission.getLessonId());
        List<Long> targetUserIds = new ArrayList<>();

        if (submission.getGroupId() != null) {
            // 小组作业，通知所有组员
            targetUserIds = groupMemberMapper.selectApprovedUserIdsByGroupId(submission.getGroupId());
        } else {
            // 个人作业，通知提交者
            targetUserIds.add(submission.getSubmitterId());
        }

        if (!targetUserIds.isEmpty()) {
            String title = "作业已评分";
            String content = String.format("您的《%s》作业已评分，等级：%s - %s",
                    lesson.getTitle(),
                    request.getGrade().name(),
                    request.getGrade().getDescription());

            notificationService.createNotifications(targetUserIds, title, content, NotificationType.GRADE_COMPLETED);
        }

        return convertToDTO(grade);
    }

    @Override
    @Transactional
    public GradeDTO updateGrade(Long id, UpdateGradeRequest request, Long graderId) {
        Grade grade = gradeMapper.selectById(id);
        if (grade == null) {
            throw new BusinessException(404, "评分记录不存在");
        }

        // 只有评分者本人可以修改
        if (!grade.getGraderId().equals(graderId)) {
            throw new BusinessException(403, "无权修改该评分");
        }

        // 更新评分
        if (request.getGrade() != null) {
            grade.setGrade(request.getGrade());
        }
        if (request.getComment() != null) {
            grade.setComment(request.getComment());
        }
        grade.setGradeTime(LocalDateTime.now());

        gradeMapper.updateById(grade);
        log.info("评分已更新: id={}, graderId={}", id, graderId);

        return convertToDTO(grade);
    }

    @Override
    @Transactional
    public void deleteGrade(Long id, Long userId) {
        Grade grade = gradeMapper.selectById(id);
        if (grade == null) {
            throw new BusinessException(404, "评分记录不存在");
        }

        // 验证权限：只有评分者本人或管理员可以删除
        if (!grade.getGraderId().equals(userId)) {
            User user = userMapper.selectById(userId);
            if (user == null || user.getRole() != UserRole.ADMIN) {
                throw new BusinessException(403, "无权删除该评分");
            }
        }

        gradeMapper.deleteById(id);
        log.info("评分已删除: id={}, userId={}", id, userId);
    }

    @Override
    public GradeDTO getGradeBySubmission(Long submissionId) {
        Grade grade = gradeMapper.findBySubmissionId(submissionId);
        if (grade == null) {
            return null;
        }
        return convertToDTO(grade);
    }

    @Override
    public List<GradeDTO> getGradesByLesson(Long lessonId) {
        List<Grade> grades = gradeMapper.findByLessonId(lessonId);
        return grades.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<GradeDTO> getGradesByStudent(Long studentId) {
        List<Grade> grades = gradeMapper.findByStudentId(studentId);
        return grades.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getLessonGradeStatistics(Long lessonId) {
        List<Grade> grades = gradeMapper.findByLessonId(lessonId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", grades.size());

        if (grades.isEmpty()) {
            stats.put("gradeDistribution", Collections.emptyMap());
            stats.put("averageGrade", null);
            return stats;
        }

        // 统计各等级分布
        Map<GradeLevel, Long> distribution = grades.stream()
                .collect(Collectors.groupingBy(Grade::getGrade, Collectors.counting()));

        Map<String, Long> distributionMap = new HashMap<>();
        distributionMap.put("A", distribution.getOrDefault(GradeLevel.A, 0L));
        distributionMap.put("B", distribution.getOrDefault(GradeLevel.B, 0L));
        distributionMap.put("C", distribution.getOrDefault(GradeLevel.C, 0L));
        distributionMap.put("D", distribution.getOrDefault(GradeLevel.D, 0L));

        stats.put("gradeDistribution", distributionMap);

        // 计算平均成绩（A=4, B=3, C=2, D=1）
        double average = grades.stream()
                .mapToInt(g -> getGradeScore(g.getGrade()))
                .average()
                .orElse(0);

        stats.put("averageGrade", Math.round(average * 100.0) / 100.0);
        stats.put("passRate", calculatePassRate(grades));

        return stats;
    }

    @Override
    public Map<String, Object> getStudentGradeStatistics(Long studentId) {
        List<Grade> grades = gradeMapper.findByStudentId(studentId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", grades.size());

        if (grades.isEmpty()) {
            stats.put("gradeDistribution", Collections.emptyMap());
            stats.put("averageGrade", null);
            return stats;
        }

        // 统计各等级分布
        Map<GradeLevel, Long> distribution = grades.stream()
                .collect(Collectors.groupingBy(Grade::getGrade, Collectors.counting()));

        Map<String, Long> distributionMap = new HashMap<>();
        distributionMap.put("A", distribution.getOrDefault(GradeLevel.A, 0L));
        distributionMap.put("B", distribution.getOrDefault(GradeLevel.B, 0L));
        distributionMap.put("C", distribution.getOrDefault(GradeLevel.C, 0L));
        distributionMap.put("D", distribution.getOrDefault(GradeLevel.D, 0L));

        stats.put("gradeDistribution", distributionMap);

        // 计算平均成绩
        double average = grades.stream()
                .mapToInt(g -> getGradeScore(g.getGrade()))
                .average()
                .orElse(0);

        stats.put("averageGrade", Math.round(average * 100.0) / 100.0);
        stats.put("passRate", calculatePassRate(grades));

        return stats;
    }

    private int getGradeScore(GradeLevel grade) {
        switch (grade) {
            case A:
                return 4;
            case B:
                return 3;
            case C:
                return 2;
            case D:
                return 1;
            default:
                return 0;
        }
    }

    private double calculatePassRate(List<Grade> grades) {
        if (grades.isEmpty()) {
            return 0;
        }

        long passCount = grades.stream()
                .filter(g -> g.getGrade() != GradeLevel.D)
                .count();

        return Math.round((double) passCount / grades.size() * 100.0 * 100.0) / 100.0;
    }

    private GradeDTO convertToDTO(Grade grade) {
        GradeDTO dto = new GradeDTO();
        dto.setId(grade.getId());
        dto.setSubmissionId(grade.getSubmissionId());
        dto.setGrade(grade.getGrade());
        dto.setGradeDescription(grade.getGrade().getDescription());
        dto.setComment(grade.getComment());
        dto.setGraderId(grade.getGraderId());
        dto.setGradeTime(grade.getGradeTime());

        // 加载评分者信息
        User grader = userMapper.selectById(grade.getGraderId());
        if (grader != null) {
            dto.setGraderName(grader.getName());
        }

        // 加载提交信息
        Submission submission = submissionMapper.selectById(grade.getSubmissionId());
        if (submission != null) {
            User submitter = userMapper.selectById(submission.getSubmitterId());
            if (submitter != null) {
                dto.setSubmitterName(submitter.getName());
            }

            Lesson lesson = lessonMapper.selectById(submission.getLessonId());
            if (lesson != null) {
                dto.setLessonTitle(lesson.getTitle());
            }
        }

        return dto;
    }
}
