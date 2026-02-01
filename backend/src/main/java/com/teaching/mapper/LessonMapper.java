package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.Lesson;
import org.apache.ibatis.annotations.Mapper;
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
}
