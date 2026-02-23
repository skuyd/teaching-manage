package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.CodeComment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 代码评论Mapper
 */
@Mapper
public interface CodeCommentMapper extends BaseMapper<CodeComment> {

    /**
     * 根据提交ID查询所有评论
     */
    @Select("SELECT * FROM t_code_comment WHERE submission_id = #{submissionId} AND del_flag = 0 ORDER BY file_path, line_number, create_time")
    List<CodeComment> findBySubmissionId(Long submissionId);

    /**
     * 根据提交ID和文件路径查询评论
     */
    @Select("SELECT * FROM t_code_comment WHERE submission_id = #{submissionId} AND file_path = #{filePath} AND del_flag = 0 ORDER BY line_number, create_time")
    List<CodeComment> findBySubmissionIdAndFilePath(Long submissionId, String filePath);

    /**
     * 根据提交ID、文件路径和行号查询评论
     */
    @Select("SELECT * FROM t_code_comment WHERE submission_id = #{submissionId} AND file_path = #{filePath} AND line_number = #{lineNumber} AND del_flag = 0 ORDER BY create_time")
    List<CodeComment> findBySubmissionIdAndFilePathAndLineNumber(Long submissionId, String filePath, Integer lineNumber);

    /**
     * 根据评论者ID查询评论
     */
    @Select("SELECT * FROM t_code_comment WHERE commenter_id = #{commenterId} AND del_flag = 0 ORDER BY create_time DESC")
    List<CodeComment> findByCommenterId(Long commenterId);

    /**
     * 根据课程ID统计评论数量
     */
    @Select("SELECT COUNT(*) FROM t_code_comment c " +
            "INNER JOIN t_submission s ON c.submission_id = s.id " +
            "WHERE s.lesson_id = #{lessonId} AND c.del_flag = 0")
    int countByLessonId(@Param("lessonId") Long lessonId);

    /**
     * 根据学科ID统计评论数量
     */
    @Select("SELECT COUNT(*) FROM t_code_comment c " +
            "INNER JOIN t_submission s ON c.submission_id = s.id " +
            "INNER JOIN t_lesson l ON s.lesson_id = l.id " +
            "WHERE l.subject_id = #{subjectId} AND c.del_flag = 0")
    int countBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * 物理删除课程下所有评论
     */
    @Delete("DELETE FROM t_code_comment WHERE submission_id IN " +
            "(SELECT id FROM t_submission WHERE lesson_id = #{lessonId})")
    int physicalDeleteByLessonId(@Param("lessonId") Long lessonId);

    /**
     * 物理删除学科下所有评论
     */
    @Delete("DELETE FROM t_code_comment WHERE submission_id IN " +
            "(SELECT s.id FROM t_submission s " +
            "INNER JOIN t_lesson l ON s.lesson_id = l.id " +
            "WHERE l.subject_id = #{subjectId})")
    int physicalDeleteBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * 物理删除提交关联的评论
     */
    @Delete("DELETE FROM t_code_comment WHERE submission_id = #{submissionId}")
    int physicalDeleteBySubmissionId(@Param("submissionId") Long submissionId);
}
