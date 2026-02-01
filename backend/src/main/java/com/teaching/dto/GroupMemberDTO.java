package com.teaching.dto;

import com.teaching.entity.GroupMember;
import com.teaching.enums.MemberStatus;
import lombok.Data;

@Data
public class GroupMemberDTO {

    private Long id;
    private Long groupId;
    private Long userId;
    private String userName;
    private MemberStatus status;
    private Boolean isLeader;

    public static GroupMemberDTO fromEntity(GroupMember member) {
        GroupMemberDTO dto = new GroupMemberDTO();
        dto.setId(member.getId());
        dto.setGroupId(member.getGroupId());
        dto.setUserId(member.getUserId());
        dto.setStatus(member.getStatus());
        return dto;
    }
}
