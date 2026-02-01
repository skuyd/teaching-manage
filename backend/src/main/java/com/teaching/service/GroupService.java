package com.teaching.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teaching.dto.CreateGroupRequest;
import com.teaching.dto.GroupDTO;
import com.teaching.entity.Group;

import java.util.List;

public interface GroupService extends IService<Group> {

    void createGroup(CreateGroupRequest request, Long userId);

    List<GroupDTO> getGroupsBySubjectId(Long subjectId);

    GroupDTO getGroupById(Long id);

    GroupDTO getMyGroupInSubject(Long subjectId, Long userId);

    void joinGroup(Long groupId, Long userId);

    void leaveGroup(Long groupId, Long userId);

    void approveMember(Long groupId, Long memberId, Long operatorId);

    void rejectMember(Long groupId, Long memberId, Long operatorId);

    void removeMember(Long groupId, Long userId, Long operatorId);

    void disbandGroup(Long groupId, Long operatorId);
}
