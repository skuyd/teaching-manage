package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.SubjectStudent;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SubjectStudentMapper extends BaseMapper<SubjectStudent> {

    @Select("SELECT COUNT(*) FROM t_subject_student " +
            "WHERE subject_id = #{subjectId} AND student_id = #{studentId} AND del_flag = 0")
    Long countBySubjectAndStudent(Long subjectId, Long studentId);

    @Select("SELECT student_id FROM t_subject_student " +
            "WHERE subject_id = #{subjectId} AND del_flag = 0")
    List<Long> selectStudentIdsBySubjectId(Long subjectId);

    @Select("SELECT subject_id FROM t_subject_student " +
            "WHERE student_id = #{studentId} AND del_flag = 0")
    List<Long> selectSubjectIdsByStudentId(Long studentId);

    /**
     * 根据学科ID统计学员数量
     */
    @Select("SELECT COUNT(*) FROM t_subject_student WHERE subject_id = #{subjectId} AND del_flag = 0")
    int countBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * 物理删除学科下所有学员关系
     */
    @Delete("DELETE FROM t_subject_student WHERE subject_id = #{subjectId}")
    int physicalDeleteBySubjectId(@Param("subjectId") Long subjectId);
}
