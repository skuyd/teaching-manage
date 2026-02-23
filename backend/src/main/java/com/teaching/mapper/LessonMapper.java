package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.Lesson;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程Mapper
 */
@Mapper
public interface LessonMapper extends BaseMapper<Lesson> {
    /**
     * 根据学科ID查询课程列表
     *
     * @param subjectId 学科ID
     * @return 课程列表
     */
    @Select("SELECT * FROM t_lesson WHERE subject_id = #{subjectId} AND del_flag = 0 ORDER BY lesson_time ASC")
    List<Lesson> selectBySubjectId(Long subjectId);

    /**
     * 根据学科ID统计课程数量
     *
     * @param subjectId 学科ID
     * @return 课程数量
     */
    @Select("SELECT COUNT(*) FROM t_lesson WHERE subject_id = #{subjectId} AND del_flag = 0")
    int countBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * 物理删除学科下所有课程
     *
     * @param subjectId 学科ID
     * @return 删除数量
     */
    @Delete("DELETE FROM t_lesson WHERE subject_id = #{subjectId}")
    int physicalDeleteBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * 物理删除课程
     *
     * @param id 课程ID
     * @return 删除数量
     */
    @Delete("DELETE FROM t_lesson WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);
}
