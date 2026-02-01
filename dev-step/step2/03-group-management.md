# 阶段二：小组管理功能

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现小组的创建、加入、退出功能，包括组长权限管理和成员人数控制。

**Architecture:** RESTful API，小组属于学科，组长可管理成员，强制分组检查。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Vue 3, Element Plus

**依赖:** 需要先完成 `02-lesson-management.md`

---

## UI 设计参考

**重要：前端页面开发必须严格参照 `UI/pencil-new.pen` 设计稿实现。**

### 本阶段相关设计页面

| 页面 | 设计稿 ID | 设计要点 |
|------|-----------|----------|
| Group Management Page | RlxWy | 小组卡片列表，成员头像展示 |

### 设计规范

- **小组卡片**: 显示小组名称、成员数量、组长标识
- **成员列表**: 头像 + 姓名，组长有特殊标识
- **申请管理**: 待审核、已通过、已拒绝状态区分
- **操作按钮**: 创建小组、申请加入、退出小组
- **人数限制提示**: 显示当前/最大人数

---

## Task 1: 创建小组实体和枚举

**Files:**
- Create: `backend/src/main/java/com/teaching/entity/Group.java`
- Create: `backend/src/main/java/com/teaching/entity/GroupMember.java`
- Create: `backend/src/main/java/com/teaching/enums/MemberStatus.java`
- Create: `backend/src/main/java/com/teaching/mapper/GroupMapper.java`
- Create: `backend/src/main/java/com/teaching/mapper/GroupMemberMapper.java`
- Test: `backend/src/test/java/com/teaching/entity/GroupTest.java`

**Step 1: 编写测试（红灯）**

```java
// backend/src/test/java/com/teaching/entity/GroupTest.java
package com.teaching.entity;

import com.teaching.enums.MemberStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroupTest {

    @Test
    @DisplayName("创建小组实体")
    void createGroup_shouldHaveAllFields() {
        Group group = new Group();
        group.setId(1L);
        group.setSubjectId(1L);
        group.setName("第一组");
        group.setLeaderId(10L);

        assertEquals(1L, group.getId());
        assertEquals("第一组", group.getName());
        assertEquals(10L, group.getLeaderId());
    }

    @Test
    @DisplayName("创建小组成员实体")
    void createGroupMember_shouldHaveAllFields() {
        GroupMember member = new GroupMember();
        member.setGroupId(1L);
        member.setUserId(10L);
        member.setStatus(MemberStatus.APPROVED);

        assertEquals(1L, member.getGroupId());
        assertEquals(10L, member.getUserId());
        assertEquals(MemberStatus.APPROVED, member.getStatus());
    }

    @Test
    @DisplayName("成员状态枚举应有三种值")
    void memberStatus_shouldHaveThreeValues() {
        assertEquals(3, MemberStatus.values().length);
        assertNotNull(MemberStatus.PENDING);
        assertNotNull(MemberStatus.APPROVED);
        assertNotNull(MemberStatus.REJECTED);
    }

    @Test
    @DisplayName("新成员默认为待审核状态")
    void newGroupMember_shouldBePendingByDefault() {
        GroupMember member = new GroupMember();
        assertEquals(MemberStatus.PENDING, member.getStatus());
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=GroupTest -q
```

**Step 3: 创建 MemberStatus 枚举**

```java
// backend/src/main/java/com/teaching/enums/MemberStatus.java
package com.teaching.enums;

public enum MemberStatus {
    PENDING,   // 待审核
    APPROVED,  // 已通过
    REJECTED   // 已拒绝
}
```

**Step 4: 创建 Group 实体**

```java
// backend/src/main/java/com/teaching/entity/Group.java
package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teaching.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_group")
public class Group extends BaseEntity {

    private Long subjectId;

    private String name;

    private Long leaderId;  // 组长用户ID
}
```

**Step 5: 创建 GroupMember 实体**

```java
// backend/src/main/java/com/teaching/entity/GroupMember.java
package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teaching.common.BaseEntity;
import com.teaching.enums.MemberStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_group_member")
public class GroupMember extends BaseEntity {

    private Long groupId;

    private Long userId;

    private MemberStatus status = MemberStatus.PENDING;
}
```

**Step 6: 创建 Mappers**

```java
// backend/src/main/java/com/teaching/mapper/GroupMapper.java
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
```

```java
// backend/src/main/java/com/teaching/mapper/GroupMemberMapper.java
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
```

**Step 7: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=GroupTest -q
```

**Step 8: Commit**

```bash
git add backend/src/
git commit -m "feat: add Group and GroupMember entities"
```

---

## Task 2: 创建小组 DTO

**Files:**
- Create: `backend/src/main/java/com/teaching/dto/GroupDTO.java`
- Create: `backend/src/main/java/com/teaching/dto/GroupMemberDTO.java`
- Create: `backend/src/main/java/com/teaching/dto/CreateGroupRequest.java`
- Create: `backend/src/main/java/com/teaching/dto/JoinGroupRequest.java`

**Step 1: 创建 GroupDTO**

```java
// backend/src/main/java/com/teaching/dto/GroupDTO.java
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
```

**Step 2: 创建 GroupMemberDTO**

```java
// backend/src/main/java/com/teaching/dto/GroupMemberDTO.java
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
```

**Step 3: 创建请求 DTO**

```java
// backend/src/main/java/com/teaching/dto/CreateGroupRequest.java
package com.teaching.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateGroupRequest {

    @NotNull(message = "学科ID不能为空")
    private Long subjectId;

    @NotBlank(message = "小组名称不能为空")
    private String name;
}
```

```java
// backend/src/main/java/com/teaching/dto/JoinGroupRequest.java
package com.teaching.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinGroupRequest {

    @NotNull(message = "小组ID不能为空")
    private Long groupId;
}
```

**Step 4: Commit**

```bash
git add backend/src/
git commit -m "feat: add Group DTOs"
```

---

## Task 3: 创建小组 Service

**Files:**
- Create: `backend/src/main/java/com/teaching/service/GroupService.java`
- Create: `backend/src/main/java/com/teaching/service/impl/GroupServiceImpl.java`
- Test: `backend/src/test/java/com/teaching/service/GroupServiceTest.java`

**Step 1: 编写 GroupService 测试（红灯）**

```java
// backend/src/test/java/com/teaching/service/GroupServiceTest.java
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

import java.util.List;

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
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=GroupServiceTest -q
```

**Step 3: 创建 GroupService 接口**

```java
// backend/src/main/java/com/teaching/service/GroupService.java
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
```

**Step 4: 创建 GroupServiceImpl**

```java
// backend/src/main/java/com/teaching/service/impl/GroupServiceImpl.java
package com.teaching.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.common.ResultCode;
import com.teaching.dto.CreateGroupRequest;
import com.teaching.dto.GroupDTO;
import com.teaching.dto.GroupMemberDTO;
import com.teaching.entity.Group;
import com.teaching.entity.GroupMember;
import com.teaching.entity.Subject;
import com.teaching.entity.User;
import com.teaching.enums.MemberStatus;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.GroupMapper;
import com.teaching.mapper.GroupMemberMapper;
import com.teaching.mapper.SubjectMapper;
import com.teaching.mapper.UserMapper;
import com.teaching.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {

    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final SubjectMapper subjectMapper;

    @Override
    @Transactional
    public void createGroup(CreateGroupRequest request, Long userId) {
        // 检查学科是否支持分组
        Subject subject = subjectMapper.selectById(request.getSubjectId());
        if (subject == null || !subject.getIsGrouped()) {
            throw new BusinessException(400, "该学科不支持分组");
        }

        // 检查用户是否已在该学科有小组
        Group existingGroup = groupMapper.selectByUserIdAndSubjectId(userId, request.getSubjectId());
        if (existingGroup != null) {
            throw new BusinessException(400, "您已在该学科有小组，不能重复创建");
        }

        // 创建小组
        Group group = new Group();
        group.setSubjectId(request.getSubjectId());
        group.setName(request.getName());
        group.setLeaderId(userId);
        groupMapper.insert(group);

        // 创建者自动成为成员（已批准）
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

        // 检查是否已申请
        GroupMember existing = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (existing != null) {
            throw new BusinessException(400, "您已申请或已在该小组中");
        }

        // 检查小组是否已满
        Long currentCount = groupMemberMapper.countApprovedByGroupId(groupId);
        if (currentCount >= subject.getMaxMembers()) {
            throw new BusinessException(400, "小组已满，无法加入");
        }

        // 创建待审核申请
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setStatus(MemberStatus.PENDING);
        groupMemberMapper.insert(member);
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

        // 删除所有成员
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId);
        groupMemberMapper.delete(wrapper);

        // 删除小组
        groupMapper.deleteById(groupId);
    }

    private GroupDTO toDTO(Group group) {
        GroupDTO dto = GroupDTO.fromEntity(group);

        // 获取成员列表
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
```

**Step 5: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=GroupServiceTest -q
```

**Step 6: Commit**

```bash
git add backend/src/
git commit -m "feat: add GroupService with member management"
```

---

## Task 4: 创建小组 Controller

**Files:**
- Create: `backend/src/main/java/com/teaching/controller/GroupController.java`
- Test: `backend/src/test/java/com/teaching/controller/GroupControllerTest.java`

由于篇幅限制，Controller 实现参考前面的模式，包含以下端点：

- `GET /api/groups/subject/{subjectId}` - 获取学科的小组列表
- `GET /api/groups/{id}` - 获取小组详情
- `GET /api/groups/my/{subjectId}` - 获取我在某学科的小组
- `POST /api/groups` - 创建小组
- `POST /api/groups/{groupId}/join` - 申请加入小组
- `POST /api/groups/{groupId}/leave` - 退出小组
- `POST /api/groups/{groupId}/members/{memberId}/approve` - 批准成员
- `POST /api/groups/{groupId}/members/{memberId}/reject` - 拒绝成员
- `DELETE /api/groups/{groupId}/members/{userId}` - 移除成员
- `DELETE /api/groups/{groupId}` - 解散小组

---

## Task 5: 前端小组管理页面

**Files:**
- Create: `frontend/src/api/group.ts`
- Create: `frontend/src/views/groups/GroupList.vue`
- Create: `frontend/src/views/groups/MyGroup.vue`

前端实现包括：
1. 学科内的小组列表展示
2. 创建小组功能
3. 申请加入小组
4. 组长管理成员（批准/拒绝/移除）
5. 我的小组页面

---

## 验证清单

1. **运行测试**
   ```bash
   cd backend && mvn test -q
   ```

2. **功能验证**
   - 在支持分组的学科中创建小组
   - 其他学员申请加入小组
   - 组长批准/拒绝申请
   - 组长移除成员
   - 成员退出小组
   - 组长解散小组

下一步：继续 `04-submission-management.md` 完成作业提交功能。
