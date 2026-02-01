package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.Submission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 作业提交Mapper
 */
@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {

    /**
     * 根据课程ID查询所有提交
     */
    @Select("SELECT * FROM t_submission WHERE lesson_id = #{lessonId} AND del_flag = 0")
    List<Submission> findByLessonId(@Param("lessonId") Long lessonId);

    /**
     * 根据课程ID和提交者ID查询提交（个人作业）
     */
    @Select("SELECT * FROM t_submission WHERE lesson_id = #{lessonId} AND submitter_id = #{submitterId} AND del_flag = 0")
    Submission findByLessonAndSubmitter(@Param("lessonId") Long lessonId, @Param("submitterId") Long submitterId);

    /**
     * 根据课程ID和小组ID查询提交（小组作业）
     */
    @Select("SELECT * FROM t_submission WHERE lesson_id = #{lessonId} AND group_id = #{groupId} AND del_flag = 0")
    Submission findByLessonAndGroup(@Param("lessonId") Long lessonId, @Param("groupId") Long groupId);

    /**
     * 根据提交者ID查询所有提交
     */
    @Select("SELECT * FROM t_submission WHERE submitter_id = #{submitterId} AND del_flag = 0 ORDER BY submit_time DESC")
    List<Submission> findBySubmitterId(@Param("submitterId") Long submitterId);

    /**
     * 根据小组ID查询所有提交
     */
    @Select("SELECT * FROM t_submission WHERE group_id = #{groupId} AND del_flag = 0 ORDER BY submit_time DESC")
    List<Submission> findByGroupId(@Param("groupId") Long groupId);
}
