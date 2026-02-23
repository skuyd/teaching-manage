package com.teaching.controller;

import com.teaching.common.Result;
import com.teaching.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();

        Integer totalUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_user WHERE del_flag = 0", Integer.class);
        Integer totalSubjects = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_subject WHERE del_flag = 0", Integer.class);
        Integer totalLessons = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_lesson WHERE del_flag = 0", Integer.class);
        Integer totalSubmissions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_submission WHERE del_flag = 0", Integer.class);

        stats.put("totalUsers", totalUsers);
        stats.put("totalSubjects", totalSubjects);
        stats.put("totalLessons", totalLessons);
        stats.put("totalSubmissions", totalSubmissions);

        return Result.success(stats);
    }

    @GetMapping("/teacher/stats")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<Map<String, Object>> getTeacherStats() {
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, Object> stats = new HashMap<>();

        // 统计教师创建的学科数
        Integer mySubjects = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_subject WHERE create_by = ? AND del_flag = 0",
                Integer.class, userId.toString());

        // 统计待评分的作业数
        Integer pendingGrades = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_submission s " +
                "LEFT JOIN t_grade g ON s.id = g.submission_id AND g.del_flag = 0 " +
                "WHERE s.del_flag = 0 AND g.id IS NULL",
                Integer.class);

        // 统计已评分的作业数
        Integer completedGrades = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_grade WHERE grader_id = ? AND del_flag = 0",
                Integer.class, userId);

        stats.put("mySubjects", mySubjects);
        stats.put("pendingGrades", pendingGrades);
        stats.put("completedGrades", completedGrades);

        return Result.success(stats);
    }

    @GetMapping("/student/stats")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Map<String, Object>> getStudentStats() {
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, Object> stats = new HashMap<>();

        // 统计学生加入的学科数
        Integer mySubjects = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_subject_student WHERE student_id = ? AND del_flag = 0",
                Integer.class, userId);

        // 统计已提交的作业数
        Integer submittedAssignments = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_submission WHERE submitter_id = ? AND del_flag = 0",
                Integer.class, userId);

        // 统计已评分的作业数
        Integer gradedAssignments = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT g.submission_id) FROM t_grade g " +
                "INNER JOIN t_submission s ON g.submission_id = s.id " +
                "WHERE s.submitter_id = ? AND g.del_flag = 0 AND s.del_flag = 0",
                Integer.class, userId);

        // 统计平均成绩（A=4, B=3, C=2, D=1）
        Double avgGrade = jdbcTemplate.queryForObject(
                "SELECT AVG(CASE g.grade " +
                "WHEN 'A' THEN 4 " +
                "WHEN 'B' THEN 3 " +
                "WHEN 'C' THEN 2 " +
                "WHEN 'D' THEN 1 " +
                "ELSE 0 END) " +
                "FROM t_grade g " +
                "INNER JOIN t_submission s ON g.submission_id = s.id " +
                "WHERE s.submitter_id = ? AND g.del_flag = 0 AND s.del_flag = 0",
                Double.class, userId);

        stats.put("mySubjects", mySubjects);
        stats.put("submittedAssignments", submittedAssignments);
        stats.put("gradedAssignments", gradedAssignments);
        stats.put("averageGrade", avgGrade != null ? Math.round(avgGrade * 100.0) / 100.0 : 0.0);

        return Result.success(stats);
    }
}
