package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.Group;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GroupMapper extends BaseMapper<Group> {

    @Select("SELECT * FROM t_group WHERE subject_id = #{subjectId} AND del_flag = 0")
    List<Group> selectBySubjectId(Long subjectId);

    @Select("SELECT g.* FROM t_group g " +
            "INNER JOIN t_group_member gm ON g.id = gm.group_id " +
            "WHERE gm.user_id = #{userId} AND gm.status = 'APPROVED' " +
            "AND g.subject_id = #{subjectId} AND g.del_flag = 0 AND gm.del_flag = 0")
    Group selectByUserIdAndSubjectId(Long userId, Long subjectId);
}
