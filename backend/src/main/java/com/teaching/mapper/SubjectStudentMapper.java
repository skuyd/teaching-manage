package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.SubjectStudent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SubjectStudentMapper extends BaseMapper<SubjectStudent> {

    @Select("SELECT COUNT(*) FROM t_subject_student " +
            "WHERE subject_id = #{subjectId} AND student_id = #{studentId} AND del_flag = 0")
    Long countBySubjectAndStudent(Long subjectId, Long studentId);
}
