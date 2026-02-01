package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;
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
}
