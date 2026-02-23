package com.teaching.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.common.ResultCode;
import com.teaching.dto.CreateGroupRequest;
import com.teaching.dto.GroupDTO;
import com.teaching.dto.GroupMemberDTO;
import com.teaching.entity.*;
import com.teaching.enums.MemberStatus;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.GroupMapper;
import com.teaching.mapper.GroupMemberMapper;
import com.teaching.mapper.SubjectMapper;
import com.teaching.mapper.UserMapper;
import com.teaching.service.GroupService;
import com.teaching.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {

    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final SubjectMapper subjectMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void createGroup(CreateGroupRequest request, Long userId) {
        Subject subject = subjectMapper.selectById(request.getSubjectId());
        if (subject == null || !subject.getIsGrouped()) {
            throw new BusinessException(400, "该学科不支持分组");
        }

        Group existingGroup = groupMapper.selectByUserIdAndSubjectId(userId, request.getSubjectId());
        if (existingGroup != null) {
            throw new BusinessException(400, "您已在该学科有小组，不能重复创建");
        }

        Group group = new Group();
        group.setSubjectId(request.getSubjectId());
        group.setName(request.getName());
        group.setLeaderId(userId);
        groupMapper.insert(group);

        GroupMember leader = new GroupMember();
        leader.setGroupId(group.getId());
        leader.setUserId(userId);
        leader.setStatus(MemberStatus.APPROVED);
        groupMemberMapper.insert(leader);
    }

    @Override
    public List<GroupDTO> getGroupsBySubjectId(Long subjectId) {
        List<Group> groups = groupMapper.selectBySubjectId(subjectId);
        return groups.stream().map(this::toDTO).toList();
    }

    @Override
    public GroupDTO getGroupById(Long id) {
        Group group = groupMapper.selectById(id);
        if (group == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "小组不存在");
        }
        return toDTO(group);
    }

    @Override
    public GroupDTO getMyGroupInSubject(Long subjectId, Long userId) {
        Group group = groupMapper.selectByUserIdAndSubjectId(userId, subjectId);
        return group != null ? toDTO(group) : null;
    }

    @Override
    @Transactional
    public void joinGroup(Long groupId, Long userId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "小组不存在");
        }

        Subject subject = subjectMapper.selectById(group.getSubjectId());

        GroupMember existing = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (existing != null) {
            throw new BusinessException(400, "您已申请或已在该小组中");
        }

        Long currentCount = groupMemberMapper.countApprovedByGroupId(groupId);
        if (currentCount >= subject.getMaxMembers()) {
            throw new BusinessException(400, "小组已满，无法加入");
        }

        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setStatus(MemberStatus.PENDING);
        groupMemberMapper.insert(member);

        // 发送小组申请通知给组长
        User applicant = userMapper.selectById(userId);
        String title = "新的小组申请";
        String content = String.format("学员《%s》申请加入小组《%s》", applicant.getName(), group.getName());
        notificationService.createNotifications(
                Collections.singletonList(group.getLeaderId()),
                title,
                content,
                NotificationType.GROUP_APPLICATION
        );
    }

    @Override
    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "小组不存在");
        }

        if (group.getLeaderId().equals(userId)) {
            throw new BusinessException(400, "组长不能退出小组，请先转让组长或解散小组");
        }

        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, userId);
        groupMemberMapper.delete(wrapper);
    }

    @Override
    @Transactional
    public void approveMember(Long groupId, Long memberId, Long operatorId) {
        Group group = groupMapper.selectById(groupId);
        if (!group.getLeaderId().equals(operatorId)) {
            throw new BusinessException(403, "只有组长可以审批成员");
        }

        Subject subject = subjectMapper.selectById(group.getSubjectId());
        Long currentCount = groupMemberMapper.countApprovedByGroupId(groupId);
        if (currentCount >= subject.getMaxMembers()) {
            throw new BusinessException(400, "小组已满，无法批准更多成员");
        }

        GroupMember member = groupMemberMapper.selectById(memberId);
        if (member == null || !member.getGroupId().equals(groupId)) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "成员申请不存在");
        }

        member.setStatus(MemberStatus.APPROVED);
        groupMemberMapper.updateById(member);

        // 发送审批通过通知给申请人
        String title = "小组申请已通过";
        String content = String.format("您加入小组《%s》的申请已通过", group.getName());
        notificationService.createNotifications(
                Collections.singletonList(member.getUserId()),
                title,
                content,
                NotificationType.GROUP_APPROVAL
        );
    }

    @Override
    @Transactional
    public void rejectMember(Long groupId, Long memberId, Long operatorId) {
        Group group = groupMapper.selectById(groupId);
        if (!group.getLeaderId().equals(operatorId)) {
            throw new BusinessException(403, "只有组长可以审批成员");
        }

        GroupMember member = groupMemberMapper.selectById(memberId);
        member.setStatus(MemberStatus.REJECTED);
        groupMemberMapper.updateById(member);

        // 发送审批拒绝通知给申请人
        String title = "小组申请已拒绝";
        String content = String.format("您加入小组《%s》的申请已被拒绝", group.getName());
        notificationService.createNotifications(
                Collections.singletonList(member.getUserId()),
                title,
                content,
                NotificationType.GROUP_APPROVAL
        );
    }

    @Override
    @Transactional
    public void removeMember(Long groupId, Long userId, Long operatorId) {
        Group group = groupMapper.selectById(groupId);
        if (!group.getLeaderId().equals(operatorId)) {
            throw new BusinessException(403, "只有组长可以移除成员");
        }

        if (userId.equals(group.getLeaderId())) {
            throw new BusinessException(400, "不能移除组长");
        }

        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, userId);
        groupMemberMapper.delete(wrapper);
    }

    @Override
    @Transactional
    public void disbandGroup(Long groupId, Long operatorId) {
        Group group = groupMapper.selectById(groupId);
        if (!group.getLeaderId().equals(operatorId)) {
            throw new BusinessException(403, "只有组长可以解散小组");
        }

        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId);
        groupMemberMapper.delete(wrapper);

        groupMapper.deleteById(groupId);
    }

    private GroupDTO toDTO(Group group) {
        GroupDTO dto = GroupDTO.fromEntity(group);

        List<GroupMember> members = groupMemberMapper.selectApprovedByGroupId(group.getId());
        dto.setMemberCount(members.size());

        List<GroupMemberDTO> memberDTOs = members.stream().map(m -> {
            GroupMemberDTO memberDTO = GroupMemberDTO.fromEntity(m);
            memberDTO.setIsLeader(m.getUserId().equals(group.getLeaderId()));
            return memberDTO;
        }).toList();
        dto.setMembers(memberDTOs);

        return dto;
    }
}
