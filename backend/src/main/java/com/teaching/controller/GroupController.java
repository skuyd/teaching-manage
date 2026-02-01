package com.teaching.controller;

import com.teaching.common.Result;
import com.teaching.dto.CreateGroupRequest;
import com.teaching.dto.GroupDTO;
import com.teaching.service.GroupService;
import com.teaching.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小组管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 获取学科的小组列表
     */
    @GetMapping("/subject/{subjectId}")
    public Result<List<GroupDTO>> listGroupsBySubject(@PathVariable Long subjectId) {
        List<GroupDTO> groups = groupService.getGroupsBySubjectId(subjectId);
        return Result.success(groups);
    }

    /**
     * 获取小组详情
     */
    @GetMapping("/{id}")
    public Result<GroupDTO> getGroupById(@PathVariable Long id) {
        GroupDTO group = groupService.getGroupById(id);
        return Result.success(group);
    }

    /**
     * 获取我在某学科的小组
     */
    @GetMapping("/my/{subjectId}")
    public Result<GroupDTO> getMyGroup(@PathVariable Long subjectId) {
        Long userId = SecurityUtils.getCurrentUserId();
        GroupDTO group = groupService.getMyGroupInSubject(subjectId, userId);
        return Result.success(group);
    }

    /**
     * 创建小组
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<Void> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        groupService.createGroup(request, userId);
        log.info("创建小组: userId={}, subjectId={}, name={}", userId, request.getSubjectId(), request.getName());
        return Result.success();
    }

    /**
     * 申请加入小组
     */
    @PostMapping("/{groupId}/join")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<Void> joinGroup(@PathVariable Long groupId) {
        Long userId = SecurityUtils.getCurrentUserId();
        groupService.joinGroup(groupId, userId);
        log.info("申请加入小组: userId={}, groupId={}", userId, groupId);
        return Result.success();
    }

    /**
     * 退出小组
     */
    @PostMapping("/{groupId}/leave")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<Void> leaveGroup(@PathVariable Long groupId) {
        Long userId = SecurityUtils.getCurrentUserId();
        groupService.leaveGroup(groupId, userId);
        log.info("退出小组: userId={}, groupId={}", userId, groupId);
        return Result.success();
    }

    /**
     * 批准成员
     */
    @PostMapping("/{groupId}/members/{memberId}/approve")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<Void> approveMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        groupService.approveMember(groupId, memberId, operatorId);
        log.info("批准成员: operatorId={}, groupId={}, memberId={}", operatorId, groupId, memberId);
        return Result.success();
    }

    /**
     * 拒绝成员
     */
    @PostMapping("/{groupId}/members/{memberId}/reject")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<Void> rejectMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        groupService.rejectMember(groupId, memberId, operatorId);
        log.info("拒绝成员: operatorId={}, groupId={}, memberId={}", operatorId, groupId, memberId);
        return Result.success();
    }

    /**
     * 移除成员
     */
    @DeleteMapping("/{groupId}/members/{userId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<Void> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        groupService.removeMember(groupId, userId, operatorId);
        log.info("移除成员: operatorId={}, groupId={}, userId={}", operatorId, groupId, userId);
        return Result.success();
    }

    /**
     * 解散小组
     */
    @DeleteMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<Void> disbandGroup(@PathVariable Long groupId) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        groupService.disbandGroup(groupId, operatorId);
        log.info("解散小组: operatorId={}, groupId={}", operatorId, groupId);
        return Result.success();
    }
}
