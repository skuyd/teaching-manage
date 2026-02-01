package com.teaching.dto;

import com.teaching.entity.Group;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupDTO {

    private Long id;
    private Long subjectId;
    private String name;
    private Long leaderId;
    private String leaderName;
    private Integer memberCount;
    private List<GroupMemberDTO> members;
    private LocalDateTime createTime;

    public static GroupDTO fromEntity(Group group) {
        GroupDTO dto = new GroupDTO();
        dto.setId(group.getId());
        dto.setSubjectId(group.getSubjectId());
        dto.setName(group.getName());
        dto.setLeaderId(group.getLeaderId());
        dto.setCreateTime(group.getCreateTime());
        return dto;
    }
}
