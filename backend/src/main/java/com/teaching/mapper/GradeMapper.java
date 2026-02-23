package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.Grade;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评分Mapper
 */
@Mapper
public interface GradeMapper extends BaseMapper<Grade> {

    /**
     * 根据提交ID查询评分
     */
    @Select("SELECT * FROM t_grade WHERE submission_id = #{submissionId} AND del_flag = 0")
    Grade findBySubmissionId(Long submissionId);

    /**
     * 根据课程ID查询所有评分
     */
    @Select("SELECT g.* FROM t_grade g " +
            "INNER JOIN t_submission s ON g.submission_id = s.id " +
            "WHERE s.lesson_id = #{lessonId} AND g.del_flag = 0 AND s.del_flag = 0")
    List<Grade> findByLessonId(Long lessonId);

    /**
     * 根据评分者ID查询评分
     */
    @Select("SELECT * FROM t_grade WHERE grader_id = #{graderId} AND del_flag = 0 ORDER BY grade_time DESC")
    List<Grade> findByGraderId(Long graderId);

    /**
     * 根据学员ID查询该学员的所有评分
     */
    @Select("SELECT g.* FROM t_grade g " +
            "INNER JOIN t_submission s ON g.submission_id = s.id " +
            "WHERE s.submitter_id = #{studentId} AND g.del_flag = 0 AND s.del_flag = 0 " +
            "ORDER BY g.grade_time DESC")
    List<Grade> findByStudentId(Long studentId);

    /**
     * 根据课程ID统计评分数量
     */
    @Select("SELECT COUNT(*) FROM t_grade g " +
            "INNER JOIN t_submission s ON g.submission_id = s.id " +
            "WHERE s.lesson_id = #{lessonId} AND g.del_flag = 0")
    int countByLessonId(@Param("lessonId") Long lessonId);

    /**
     * 根据学科ID统计评分数量
     */
    @Select("SELECT COUNT(*) FROM t_grade g " +
            "INNER JOIN t_submission s ON g.submission_id = s.id " +
            "INNER JOIN t_lesson l ON s.lesson_id = l.id " +
            "WHERE l.subject_id = #{subjectId} AND g.del_flag = 0")
    int countBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * 物理删除课程下所有评分
     */
    @Delete("DELETE FROM t_grade WHERE submission_id IN " +
            "(SELECT id FROM t_submission WHERE lesson_id = #{lessonId})")
    int physicalDeleteByLessonId(@Param("lessonId") Long lessonId);

    /**
     * 物理删除学科下所有评分
     */
    @Delete("DELETE FROM t_grade WHERE submission_id IN " +
            "(SELECT s.id FROM t_submission s " +
            "INNER JOIN t_lesson l ON s.lesson_id = l.id " +
            "WHERE l.subject_id = #{subjectId})")
    int physicalDeleteBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * 物理删除提交关联的评分
     */
    @Delete("DELETE FROM t_grade WHERE submission_id = #{submissionId}")
    int physicalDeleteBySubmissionId(@Param("submissionId") Long submissionId);
}
