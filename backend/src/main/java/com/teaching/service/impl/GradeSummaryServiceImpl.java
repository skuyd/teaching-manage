package com.teaching.service.impl;

import com.teaching.dto.GradeSummaryDTO;
import com.teaching.dto.LessonGradeSummaryDTO;
import com.teaching.dto.StudentGradeSummaryDTO;
import com.teaching.entity.Grade;
import com.teaching.entity.Lesson;
import com.teaching.entity.Submission;
import com.teaching.entity.User;
import com.teaching.enums.GradeLevel;
import com.teaching.mapper.GradeMapper;
import com.teaching.mapper.LessonMapper;
import com.teaching.mapper.SubmissionMapper;
import com.teaching.mapper.UserMapper;
import com.teaching.service.GradeSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradeSummaryServiceImpl implements GradeSummaryService {

    private final GradeMapper gradeMapper;
    private final LessonMapper lessonMapper;
    private final SubmissionMapper submissionMapper;
    private final UserMapper userMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public GradeSummaryDTO getSubjectGradeSummary(Long subjectId) {
        // Get all lessons for this subject
        List<Lesson> lessons = lessonMapper.selectBySubjectId(subjectId);

        GradeSummaryDTO summary = new GradeSummaryDTO();
        summary.setSubjectId(subjectId);
        summary.setTotalLessons(lessons.size());

        // Get lesson summaries
        List<LessonGradeSummaryDTO> lessonSummaries = lessons.stream()
                .map(lesson -> getLessonGradeSummary(lesson.getId()))
                .collect(Collectors.toList());
        summary.setLessonSummaries(lessonSummaries);

        // Calculate overall statistics
        GradeSummaryDTO.OverallStatistics overallStats = new GradeSummaryDTO.OverallStatistics();

        List<Grade> allGrades = lessons.stream()
                .flatMap(lesson -> gradeMapper.findByLessonId(lesson.getId()).stream())
                .collect(Collectors.toList());

        overallStats.setTotalGrades(allGrades.size());

        if (!allGrades.isEmpty()) {
            // Grade distribution
            Map<GradeLevel, Long> distribution = allGrades.stream()
                    .collect(Collectors.groupingBy(Grade::getGrade, Collectors.counting()));

            Map<String, Long> distributionMap = new HashMap<>();
            distributionMap.put("A", distribution.getOrDefault(GradeLevel.A, 0L));
            distributionMap.put("B", distribution.getOrDefault(GradeLevel.B, 0L));
            distributionMap.put("C", distribution.getOrDefault(GradeLevel.C, 0L));
            distributionMap.put("D", distribution.getOrDefault(GradeLevel.D, 0L));
            overallStats.setGradeDistribution(distributionMap);

            // Average grade
            double average = allGrades.stream()
                    .mapToInt(g -> getGradeScore(g.getGrade()))
                    .average()
                    .orElse(0);
            overallStats.setAverageGrade(Math.round(average * 100.0) / 100.0);

            // Pass rate
            long passCount = allGrades.stream()
                    .filter(g -> g.getGrade() != GradeLevel.D)
                    .count();
            double passRate = (double) passCount / allGrades.size() * 100.0;
            overallStats.setPassRate(Math.round(passRate * 100.0) / 100.0);
        }

        summary.setOverallStatistics(overallStats);

        return summary;
    }

    @Override
    public LessonGradeSummaryDTO getLessonGradeSummary(Long lessonId) {
        Lesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null) {
            return null;
        }

        LessonGradeSummaryDTO summary = new LessonGradeSummaryDTO();
        summary.setLessonId(lesson.getId());
        summary.setLessonTitle(lesson.getTitle());
        summary.setLessonTime(lesson.getLessonTime().format(DATE_FORMATTER));
        if (lesson.getDeadline() != null) {
            summary.setDeadline(lesson.getDeadline().format(DATE_FORMATTER));
        }

        // Get submissions and grades
        List<Submission> submissions = submissionMapper.findByLessonId(lessonId);
        List<Grade> grades = gradeMapper.findByLessonId(lessonId);

        summary.setSubmittedCount(submissions.size());
        summary.setGradedCount(grades.size());
        summary.setUngradedCount(submissions.size() - grades.size());

        // Calculate statistics
        if (!grades.isEmpty()) {
            Map<GradeLevel, Long> distribution = grades.stream()
                    .collect(Collectors.groupingBy(Grade::getGrade, Collectors.counting()));

            Map<String, Long> distributionMap = new HashMap<>();
            distributionMap.put("A", distribution.getOrDefault(GradeLevel.A, 0L));
            distributionMap.put("B", distribution.getOrDefault(GradeLevel.B, 0L));
            distributionMap.put("C", distribution.getOrDefault(GradeLevel.C, 0L));
            distributionMap.put("D", distribution.getOrDefault(GradeLevel.D, 0L));
            summary.setGradeDistribution(distributionMap);

            double average = grades.stream()
                    .mapToInt(g -> getGradeScore(g.getGrade()))
                    .average()
                    .orElse(0);
            summary.setAverageGrade(Math.round(average * 100.0) / 100.0);

            long passCount = grades.stream()
                    .filter(g -> g.getGrade() != GradeLevel.D)
                    .count();
            double passRate = (double) passCount / grades.size() * 100.0;
            summary.setPassRate(Math.round(passRate * 100.0) / 100.0);
        }

        // Build student grade details
        Map<Long, Grade> gradeMap = grades.stream()
                .collect(Collectors.toMap(Grade::getSubmissionId, g -> g));
        Map<Long, Submission> submissionMap = submissions.stream()
                .collect(Collectors.toMap(Submission::getSubmitterId, s -> s));

        List<LessonGradeSummaryDTO.StudentGradeDetail> studentDetails = new ArrayList<>();
        Set<Long> processedStudents = new HashSet<>();

        for (Submission submission : submissions) {
            Long studentId = submission.getSubmitterId();
            if (processedStudents.contains(studentId)) {
                continue;
            }
            processedStudents.add(studentId);

            LessonGradeSummaryDTO.StudentGradeDetail detail = new LessonGradeSummaryDTO.StudentGradeDetail();
            detail.setStudentId(studentId);

            User student = userMapper.selectById(studentId);
            if (student != null) {
                detail.setStudentName(student.getName());
            }

            detail.setSubmitted(true);
            detail.setSubmitTime(submission.getSubmitTime().format(DATE_FORMATTER));

            Grade grade = gradeMap.get(submission.getId());
            if (grade != null) {
                detail.setGraded(true);
                detail.setGrade(grade.getGrade());
                detail.setGradeDescription(grade.getGrade().getDescription());
                detail.setComment(grade.getComment());
                detail.setGradeTime(grade.getGradeTime().format(DATE_FORMATTER));
            } else {
                detail.setGraded(false);
            }

            studentDetails.add(detail);
        }

        summary.setStudentGrades(studentDetails);

        return summary;
    }

    @Override
    public StudentGradeSummaryDTO getStudentGradeSummary(Long studentId) {
        User student = userMapper.selectById(studentId);
        if (student == null) {
            return null;
        }

        StudentGradeSummaryDTO summary = new StudentGradeSummaryDTO();
        summary.setStudentId(studentId);
        summary.setStudentName(student.getName());
        summary.setStudentEmail(student.getEmail());

        // Get all grades for this student
        List<Grade> grades = gradeMapper.findByStudentId(studentId);
        summary.setGradedCount(grades.size());

        if (!grades.isEmpty()) {
            Map<GradeLevel, Long> distribution = grades.stream()
                    .collect(Collectors.groupingBy(Grade::getGrade, Collectors.counting()));

            Map<String, Long> distributionMap = new HashMap<>();
            distributionMap.put("A", distribution.getOrDefault(GradeLevel.A, 0L));
            distributionMap.put("B", distribution.getOrDefault(GradeLevel.B, 0L));
            distributionMap.put("C", distribution.getOrDefault(GradeLevel.C, 0L));
            distributionMap.put("D", distribution.getOrDefault(GradeLevel.D, 0L));
            summary.setGradeDistribution(distributionMap);

            double average = grades.stream()
                    .mapToInt(g -> getGradeScore(g.getGrade()))
                    .average()
                    .orElse(0);
            summary.setAverageGrade(Math.round(average * 100.0) / 100.0);

            long passCount = grades.stream()
                    .filter(g -> g.getGrade() != GradeLevel.D)
                    .count();
            double passRate = (double) passCount / grades.size() * 100.0;
            summary.setPassRate(Math.round(passRate * 100.0) / 100.0);
        }

        // Build lesson grade details
        List<StudentGradeSummaryDTO.LessonGradeDetail> lessonDetails = new ArrayList<>();
        for (Grade grade : grades) {
            Submission submission = submissionMapper.selectById(grade.getSubmissionId());
            if (submission == null) {
                continue;
            }

            Lesson lesson = lessonMapper.selectById(submission.getLessonId());
            if (lesson == null) {
                continue;
            }

            StudentGradeSummaryDTO.LessonGradeDetail detail = new StudentGradeSummaryDTO.LessonGradeDetail();
            detail.setLessonId(lesson.getId());
            detail.setLessonTitle(lesson.getTitle());
            detail.setLessonTime(lesson.getLessonTime().format(DATE_FORMATTER));
            detail.setGrade(grade.getGrade());
            detail.setGradeDescription(grade.getGrade().getDescription());
            detail.setComment(grade.getComment());
            detail.setGradeTime(grade.getGradeTime().format(DATE_FORMATTER));
            detail.setSubmitted(true);
            detail.setSubmitTime(submission.getSubmitTime().format(DATE_FORMATTER));

            User grader = userMapper.selectById(grade.getGraderId());
            if (grader != null) {
                detail.setGraderName(grader.getName());
            }

            lessonDetails.add(detail);
        }

        summary.setLessonGrades(lessonDetails);

        return summary;
    }

    @Override
    public byte[] exportSubjectGradesToExcel(Long subjectId) throws IOException {
        GradeSummaryDTO summary = getSubjectGradeSummary(subjectId);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Create summary sheet
            Sheet summarySheet = workbook.createSheet("汇总");
            createSummarySheet(workbook, summarySheet, summary);

            // Create lesson sheets
            for (LessonGradeSummaryDTO lessonSummary : summary.getLessonSummaries()) {
                String sheetName = sanitizeSheetName(lessonSummary.getLessonTitle());
                Sheet lessonSheet = workbook.createSheet(sheetName);
                createLessonSheet(workbook, lessonSheet, lessonSummary);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Override
    public byte[] exportLessonGradesToExcel(Long lessonId) throws IOException {
        LessonGradeSummaryDTO summary = getLessonGradeSummary(lessonId);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(summary.getLessonTitle());
            createLessonSheet(workbook, sheet, summary);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Override
    public byte[] exportStudentGradesToExcel(Long studentId) throws IOException {
        StudentGradeSummaryDTO summary = getStudentGradeSummary(studentId);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(summary.getStudentName());
            createStudentSheet(workbook, sheet, summary);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void createSummarySheet(Workbook workbook, Sheet sheet, GradeSummaryDTO summary) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("学科成绩汇总");
        titleCell.setCellStyle(headerStyle);

        rowNum++; // Empty row

        // Overall statistics
        GradeSummaryDTO.OverallStatistics stats = summary.getOverallStatistics();
        if (stats != null) {
            createDataRow(sheet, rowNum++, dataStyle, "总评分数", String.valueOf(stats.getTotalGrades()));
            createDataRow(sheet, rowNum++, dataStyle, "平均成绩", String.format("%.2f", stats.getAverageGrade()));
            createDataRow(sheet, rowNum++, dataStyle, "及格率", String.format("%.2f%%", stats.getPassRate()));

            rowNum++; // Empty row

            Row distRow = sheet.createRow(rowNum++);
            distRow.createCell(0).setCellValue("等级分布");
            createDataRow(sheet, rowNum++, dataStyle, "A", String.valueOf(stats.getGradeDistribution().get("A")));
            createDataRow(sheet, rowNum++, dataStyle, "B", String.valueOf(stats.getGradeDistribution().get("B")));
            createDataRow(sheet, rowNum++, dataStyle, "C", String.valueOf(stats.getGradeDistribution().get("C")));
            createDataRow(sheet, rowNum++, dataStyle, "D", String.valueOf(stats.getGradeDistribution().get("D")));
        }

        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createLessonSheet(Workbook workbook, Sheet sheet, LessonGradeSummaryDTO summary) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int rowNum = 0;

        // Header row
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"学员姓名", "评分等级", "评分描述", "评语", "提交时间", "评分时间"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        if (summary.getStudentGrades() != null) {
            for (LessonGradeSummaryDTO.StudentGradeDetail detail : summary.getStudentGrades()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(detail.getStudentName());
                row.createCell(1).setCellValue(detail.getGrade() != null ? detail.getGrade().name() : "");
                row.createCell(2).setCellValue(detail.getGradeDescription() != null ? detail.getGradeDescription() : "");
                row.createCell(3).setCellValue(detail.getComment() != null ? detail.getComment() : "");
                row.createCell(4).setCellValue(detail.getSubmitTime() != null ? detail.getSubmitTime() : "");
                row.createCell(5).setCellValue(detail.getGradeTime() != null ? detail.getGradeTime() : "");

                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createStudentSheet(Workbook workbook, Sheet sheet, StudentGradeSummaryDTO summary) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int rowNum = 0;

        // Header row
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"课程标题", "课程时间", "评分等级", "评分描述", "评语", "评分者", "提交时间", "评分时间"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        if (summary.getLessonGrades() != null) {
            for (StudentGradeSummaryDTO.LessonGradeDetail detail : summary.getLessonGrades()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(detail.getLessonTitle());
                row.createCell(1).setCellValue(detail.getLessonTime());
                row.createCell(2).setCellValue(detail.getGrade().name());
                row.createCell(3).setCellValue(detail.getGradeDescription());
                row.createCell(4).setCellValue(detail.getComment() != null ? detail.getComment() : "");
                row.createCell(5).setCellValue(detail.getGraderName());
                row.createCell(6).setCellValue(detail.getSubmitTime());
                row.createCell(7).setCellValue(detail.getGradeTime());

                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void createDataRow(Sheet sheet, int rowNum, CellStyle style, String label, String value) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(style);
    }

    private String sanitizeSheetName(String name) {
        // Excel sheet names cannot contain: \ / ? * [ ]
        // Max length is 31 characters
        String sanitized = name.replaceAll("[\\\\/:*?\\[\\]]", "_");
        if (sanitized.length() > 31) {
            sanitized = sanitized.substring(0, 31);
        }
        return sanitized;
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
}
