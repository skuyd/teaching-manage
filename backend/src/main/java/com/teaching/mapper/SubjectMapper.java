package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.Subject;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SubjectMapper extends BaseMapper<Subject> {

    @Select("SELECT s.* FROM t_subject s " +
            "INNER JOIN t_subject_student ss ON s.id = ss.subject_id " +
            "WHERE ss.student_id = #{studentId} AND s.del_flag = 0 AND ss.del_flag = 0")
    List<Subject> selectByStudentId(Long studentId);

    /**
     * 物理删除学科
     */
    @Delete("DELETE FROM t_subject WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);
}
