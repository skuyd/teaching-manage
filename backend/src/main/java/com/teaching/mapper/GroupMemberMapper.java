package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.GroupMember;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {

    @Select("SELECT * FROM t_group_member WHERE group_id = #{groupId} AND status = 'APPROVED' AND del_flag = 0")
    List<GroupMember> selectApprovedByGroupId(Long groupId);

    @Select("SELECT COUNT(*) FROM t_group_member WHERE group_id = #{groupId} AND status = 'APPROVED' AND del_flag = 0")
    Long countApprovedByGroupId(Long groupId);

    @Select("SELECT * FROM t_group_member WHERE group_id = #{groupId} AND user_id = #{userId} AND del_flag = 0")
    GroupMember selectByGroupIdAndUserId(Long groupId, Long userId);

    @Select("SELECT user_id FROM t_group_member WHERE group_id = #{groupId} AND status = 'APPROVED' AND del_flag = 0")
    List<Long> selectApprovedUserIdsByGroupId(Long groupId);

    /**
     * 物理删除小组下所有成员关系
     */
    @Delete("DELETE FROM t_group_member WHERE group_id = #{groupId}")
    int physicalDeleteByGroupId(@Param("groupId") Long groupId);

    /**
     * 物理删除学科下所有小组成员关系
     */
    @Delete("DELETE FROM t_group_member WHERE group_id IN " +
            "(SELECT id FROM t_group WHERE subject_id = #{subjectId})")
    int physicalDeleteBySubjectId(@Param("subjectId") Long subjectId);
}
