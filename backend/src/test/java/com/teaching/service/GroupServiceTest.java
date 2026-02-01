package com.teaching.service;

import com.teaching.dto.CreateGroupRequest;
import com.teaching.entity.Group;
import com.teaching.entity.GroupMember;
import com.teaching.entity.Subject;
import com.teaching.enums.MemberStatus;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.GroupMapper;
import com.teaching.mapper.GroupMemberMapper;
import com.teaching.mapper.SubjectMapper;
import com.teaching.service.impl.GroupServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private GroupMemberMapper groupMemberMapper;

    @Mock
    private SubjectMapper subjectMapper;

    private GroupServiceImpl groupService;

    @BeforeEach
    void setUp() {
        groupService = new GroupServiceImpl(groupMapper, groupMemberMapper, subjectMapper);
    }

    @Test
    @DisplayName("创建小组并自动加入创建者为组长")
    void createGroup_shouldAddCreatorAsLeader() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setIsGrouped(true);
        subject.setMinMembers(3);
        subject.setMaxMembers(5);

        when(subjectMapper.selectById(1L)).thenReturn(subject);
        when(groupMapper.selectByUserIdAndSubjectId(10L, 1L)).thenReturn(null);
        when(groupMapper.insert(any(Group.class))).thenAnswer(inv -> {
            Group g = inv.getArgument(0);
            g.setId(1L);
            return 1;
        });
        when(groupMemberMapper.insert(any(GroupMember.class))).thenReturn(1);

        CreateGroupRequest request = new CreateGroupRequest();
        request.setSubjectId(1L);
        request.setName("第一组");

        groupService.createGroup(request, 10L);

        verify(groupMapper).insert(argThat(g ->
                g.getName().equals("第一组") && g.getLeaderId() == 10L
        ));
        verify(groupMemberMapper).insert(argThat(m ->
                m.getUserId() == 10L && m.getStatus() == MemberStatus.APPROVED
        ));
    }

    @Test
    @DisplayName("用户已在该学科有小组时不能创建新小组")
    void createGroup_shouldFailIfAlreadyInGroup() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setIsGrouped(true);

        Group existingGroup = new Group();
        existingGroup.setId(1L);

        when(subjectMapper.selectById(1L)).thenReturn(subject);
        when(groupMapper.selectByUserIdAndSubjectId(10L, 1L)).thenReturn(existingGroup);

        CreateGroupRequest request = new CreateGroupRequest();
        request.setSubjectId(1L);
        request.setName("新小组");

        assertThrows(BusinessException.class, () -> groupService.createGroup(request, 10L));
    }

    @Test
    @DisplayName("申请加入小组")
    void joinGroup_shouldCreatePendingMember() {
        Group group = new Group();
        group.setId(1L);
        group.setSubjectId(1L);

        Subject subject = new Subject();
        subject.setMaxMembers(5);

        when(groupMapper.selectById(1L)).thenReturn(group);
        when(subjectMapper.selectById(1L)).thenReturn(subject);
        when(groupMemberMapper.selectByGroupIdAndUserId(1L, 10L)).thenReturn(null);
        when(groupMemberMapper.countApprovedByGroupId(1L)).thenReturn(3L);
        when(groupMemberMapper.insert(any(GroupMember.class))).thenReturn(1);

        groupService.joinGroup(1L, 10L);

        verify(groupMemberMapper).insert(argThat(m ->
                m.getUserId() == 10L && m.getStatus() == MemberStatus.PENDING
        ));
    }

    @Test
    @DisplayName("小组已满时不能加入")
    void joinGroup_shouldFailIfGroupFull() {
        Group group = new Group();
        group.setId(1L);
        group.setSubjectId(1L);

        Subject subject = new Subject();
        subject.setMaxMembers(3);

        when(groupMapper.selectById(1L)).thenReturn(group);
        when(subjectMapper.selectById(1L)).thenReturn(subject);
        when(groupMemberMapper.selectByGroupIdAndUserId(1L, 10L)).thenReturn(null);
        when(groupMemberMapper.countApprovedByGroupId(1L)).thenReturn(3L);

        assertThrows(BusinessException.class, () -> groupService.joinGroup(1L, 10L));
    }

    @Test
    @DisplayName("组长可以批准成员申请")
    void approveMember_shouldUpdateStatus() {
        Group group = new Group();
        group.setId(1L);
        group.setLeaderId(10L);
        group.setSubjectId(1L);

        Subject subject = new Subject();
        subject.setMaxMembers(5);

        GroupMember member = new GroupMember();
        member.setId(1L);
        member.setGroupId(1L);
        member.setUserId(20L);
        member.setStatus(MemberStatus.PENDING);

        when(groupMapper.selectById(1L)).thenReturn(group);
        when(subjectMapper.selectById(1L)).thenReturn(subject);
        when(groupMemberMapper.selectById(1L)).thenReturn(member);
        when(groupMemberMapper.countApprovedByGroupId(1L)).thenReturn(2L);
        when(groupMemberMapper.updateById(any(GroupMember.class))).thenReturn(1);

        groupService.approveMember(1L, 1L, 10L);

        verify(groupMemberMapper).updateById(argThat(m ->
                m.getStatus() == MemberStatus.APPROVED
        ));
    }

    @Test
    @DisplayName("非组长不能批准成员")
    void approveMember_shouldFailIfNotLeader() {
        Group group = new Group();
        group.setId(1L);
        group.setLeaderId(10L);

        when(groupMapper.selectById(1L)).thenReturn(group);

        assertThrows(BusinessException.class, () -> groupService.approveMember(1L, 1L, 99L));
    }
}
