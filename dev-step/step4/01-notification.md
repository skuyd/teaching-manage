# 阶段四：站内通知系统

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现站内通知系统，支持新课程发布、作业截止提醒、评分完成、小组申请等通知。

**Architecture:** RESTful API，SSE（Server-Sent Events）实时推送，定时任务触发截止提醒。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Vue 3, Element Plus

**依赖:** 需要先完成阶段三

---

## UI 设计参考

**重要：前端页面开发必须严格参照 `UI/pencil-new.pen` 设计稿实现。**

### 本阶段相关设计页面

| 页面 | 设计稿 ID | 设计要点 |
|------|-----------|----------|
| Notification Center Page | GJbFq | 通知列表，未读标记，分类筛选 |

### 设计规范

- **通知铃铛**: 顶部导航栏，显示未读数量红点
- **通知列表**: 按时间倒序，未读加粗显示
- **通知类型图标**: 不同类型不同图标和颜色
- **标记已读**: 单条标记和全部已读
- **通知详情**: 点击展开或跳转相关页面

---

## Task 1: 创建通知实体和枚举

**Files:**
- Create: `backend/src/main/java/com/teaching/entity/Notification.java`
- Create: `backend/src/main/java/com/teaching/enums/NotificationType.java`
- Create: `backend/src/main/java/com/teaching/mapper/NotificationMapper.java`
- Test: `backend/src/test/java/com/teaching/entity/NotificationTest.java`

**Step 1: 编写测试（红灯）**

```java
// backend/src/test/java/com/teaching/entity/NotificationTest.java
package com.teaching.entity;

import com.teaching.enums.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    @DisplayName("创建通知实体")
    void createNotification_shouldHaveAllFields() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUserId(10L);
        notification.setTitle("新课程发布");
        notification.setContent("Java基础第三课已发布");
        notification.setType(NotificationType.LESSON_PUBLISHED);
        notification.setIsRead(false);

        assertEquals(1L, notification.getId());
        assertEquals(10L, notification.getUserId());
        assertEquals("新课程发布", notification.getTitle());
        assertFalse(notification.getIsRead());
    }

    @Test
    @DisplayName("通知类型枚举应有所有值")
    void notificationType_shouldHaveAllValues() {
        assertEquals(5, NotificationType.values().length);
        assertNotNull(NotificationType.LESSON_PUBLISHED);
        assertNotNull(NotificationType.HOMEWORK_DEADLINE);
        assertNotNull(NotificationType.GRADE_COMPLETED);
        assertNotNull(NotificationType.GROUP_APPLICATION);
        assertNotNull(NotificationType.GROUP_APPROVED);
    }

    @Test
    @DisplayName("新通知默认未读")
    void newNotification_shouldBeUnread() {
        Notification notification = new Notification();
        assertFalse(notification.getIsRead());
    }
}
```

**Step 2: 创建 NotificationType 枚举**

```java
// backend/src/main/java/com/teaching/enums/NotificationType.java
package com.teaching.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
    LESSON_PUBLISHED("新课程发布"),
    HOMEWORK_DEADLINE("作业截止提醒"),
    GRADE_COMPLETED("评分完成"),
    GROUP_APPLICATION("小组申请"),
    GROUP_APPROVED("申请通过");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }
}
```

**Step 3: 创建 Notification 实体**

```java
// backend/src/main/java/com/teaching/entity/Notification.java
package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teaching.common.BaseEntity;
import com.teaching.enums.NotificationType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_notification")
public class Notification extends BaseEntity {

    private Long userId;              // 接收用户ID

    private String title;             // 通知标题

    private String content;           // 通知内容

    private NotificationType type;    // 通知类型

    private Boolean isRead = false;   // 是否已读

    private Long relatedId;           // 关联对象ID（课程ID、提交ID等）

    private String relatedType;       // 关联对象类型（LESSON、SUBMISSION、GROUP）
}
```

**Step 4: 创建 NotificationMapper**

```java
// backend/src/main/java/com/teaching/mapper/NotificationMapper.java
package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Select("SELECT * FROM t_notification WHERE user_id = #{userId} AND del_flag = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<Notification> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT * FROM t_notification WHERE user_id = #{userId} AND is_read = 0 AND del_flag = 0 ORDER BY create_time DESC")
    List<Notification> selectUnreadByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM t_notification WHERE user_id = #{userId} AND is_read = 0 AND del_flag = 0")
    Long countUnreadByUserId(Long userId);

    @Update("UPDATE t_notification SET is_read = 1 WHERE user_id = #{userId} AND del_flag = 0")
    int markAllAsRead(Long userId);

    @Update("UPDATE t_notification SET is_read = 1 WHERE id = #{id}")
    int markAsRead(Long id);
}
```

**Step 5: Commit**

```bash
git add backend/src/
git commit -m "feat: add Notification entity and mapper"
```

---

## Task 2: 创建通知 Service

**Files:**
- Create: `backend/src/main/java/com/teaching/service/NotificationService.java`
- Create: `backend/src/main/java/com/teaching/service/impl/NotificationServiceImpl.java`
- Create: `backend/src/main/java/com/teaching/dto/NotificationDTO.java`
- Test: `backend/src/test/java/com/teaching/service/NotificationServiceTest.java`

**Step 1: 创建 NotificationDTO**

```java
// backend/src/main/java/com/teaching/dto/NotificationDTO.java
package com.teaching.dto;

import com.teaching.entity.Notification;
import com.teaching.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private NotificationType type;
    private String typeDescription;
    private Boolean isRead;
    private Long relatedId;
    private String relatedType;
    private LocalDateTime createTime;

    public static NotificationDTO fromEntity(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setType(notification.getType());
        dto.setTypeDescription(notification.getType().getDescription());
        dto.setIsRead(notification.getIsRead());
        dto.setRelatedId(notification.getRelatedId());
        dto.setRelatedType(notification.getRelatedType());
        dto.setCreateTime(notification.getCreateTime());
        return dto;
    }
}
```

**Step 2: 创建 NotificationService 接口**

```java
// backend/src/main/java/com/teaching/service/NotificationService.java
package com.teaching.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teaching.dto.NotificationDTO;
import com.teaching.entity.Notification;
import com.teaching.enums.NotificationType;

import java.util.List;

public interface NotificationService extends IService<Notification> {

    /**
     * 发送通知给单个用户
     */
    void send(Long userId, NotificationType type, String title, String content, Long relatedId, String relatedType);

    /**
     * 发送通知给多个用户
     */
    void sendToUsers(List<Long> userIds, NotificationType type, String title, String content, Long relatedId, String relatedType);

    /**
     * 获取用户的通知列表
     */
    List<NotificationDTO> getNotifications(Long userId, int limit);

    /**
     * 获取未读通知
     */
    List<NotificationDTO> getUnreadNotifications(Long userId);

    /**
     * 获取未读数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 标记为已读
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 标记全部为已读
     */
    void markAllAsRead(Long userId);

    // === 业务通知方法 ===

    /**
     * 发送新课程发布通知
     */
    void notifyLessonPublished(Long lessonId, Long subjectId);

    /**
     * 发送作业截止提醒
     */
    void notifyHomeworkDeadline(Long lessonId);

    /**
     * 发送评分完成通知
     */
    void notifyGradeCompleted(Long submissionId);

    /**
     * 发送小组申请通知（给组长）
     */
    void notifyGroupApplication(Long groupId, Long applicantId);

    /**
     * 发送申请通过通知
     */
    void notifyGroupApproved(Long groupId, Long memberId);
}
```

**Step 3: 创建 NotificationServiceImpl**

```java
// backend/src/main/java/com/teaching/service/impl/NotificationServiceImpl.java
package com.teaching.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.dto.NotificationDTO;
import com.teaching.entity.*;
import com.teaching.enums.NotificationType;
import com.teaching.mapper.*;
import com.teaching.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final LessonMapper lessonMapper;
    private final SubjectMapper subjectMapper;
    private final SubjectStudentMapper subjectStudentMapper;
    private final SubmissionMapper submissionMapper;
    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void send(Long userId, NotificationType type, String title, String content, Long relatedId, String relatedType) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setIsRead(false);
        notificationMapper.insert(notification);
    }

    @Override
    @Transactional
    public void sendToUsers(List<Long> userIds, NotificationType type, String title, String content, Long relatedId, String relatedType) {
        for (Long userId : userIds) {
            send(userId, type, title, content, relatedId, relatedType);
        }
    }

    @Override
    public List<NotificationDTO> getNotifications(Long userId, int limit) {
        List<Notification> notifications = notificationMapper.selectByUserId(userId, limit);
        return notifications.stream().map(NotificationDTO::fromEntity).toList();
    }

    @Override
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationMapper.selectUnreadByUserId(userId);
        return notifications.stream().map(NotificationDTO::fromEntity).toList();
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notificationMapper.markAsRead(notificationId);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    // === 业务通知方法 ===

    @Override
    public void notifyLessonPublished(Long lessonId, Long subjectId) {
        Lesson lesson = lessonMapper.selectById(lessonId);
        Subject subject = subjectMapper.selectById(subjectId);

        String title = "新课程发布";
        String content = String.format("【%s】发布了新课程：%s", subject.getName(), lesson.getTitle());

        // 获取学科下所有学员
        List<Long> studentIds = subjectStudentMapper.selectStudentIdsBySubjectId(subjectId);
        sendToUsers(studentIds, NotificationType.LESSON_PUBLISHED, title, content, lessonId, "LESSON");
    }

    @Override
    public void notifyHomeworkDeadline(Long lessonId) {
        Lesson lesson = lessonMapper.selectById(lessonId);
        Subject subject = subjectMapper.selectById(lesson.getSubjectId());

        String title = "作业截止提醒";
        String content = String.format("【%s - %s】的作业将于明天截止，请尽快提交！", subject.getName(), lesson.getTitle());

        // 获取学科下所有学员
        List<Long> studentIds = subjectStudentMapper.selectStudentIdsBySubjectId(lesson.getSubjectId());
        sendToUsers(studentIds, NotificationType.HOMEWORK_DEADLINE, title, content, lessonId, "LESSON");
    }

    @Override
    public void notifyGradeCompleted(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        Lesson lesson = lessonMapper.selectById(submission.getLessonId());

        String title = "作业已评分";
        String content = String.format("您提交的【%s】作业已完成评分，快去查看吧！", lesson.getTitle());

        // 发送给提交者
        send(submission.getSubmitterId(), NotificationType.GRADE_COMPLETED, title, content, submissionId, "SUBMISSION");

        // 如果是小组作业，发送给所有组员
        if (submission.getGroupId() != null) {
            List<GroupMember> members = groupMemberMapper.selectApprovedByGroupId(submission.getGroupId());
            for (GroupMember member : members) {
                if (!member.getUserId().equals(submission.getSubmitterId())) {
                    send(member.getUserId(), NotificationType.GRADE_COMPLETED, title, content, submissionId, "SUBMISSION");
                }
            }
        }
    }

    @Override
    public void notifyGroupApplication(Long groupId, Long applicantId) {
        Group group = groupMapper.selectById(groupId);
        User applicant = userMapper.selectById(applicantId);

        String title = "新的入组申请";
        String content = String.format("【%s】申请加入小组【%s】", applicant.getName(), group.getName());

        // 发送给组长
        send(group.getLeaderId(), NotificationType.GROUP_APPLICATION, title, content, groupId, "GROUP");
    }

    @Override
    public void notifyGroupApproved(Long groupId, Long memberId) {
        Group group = groupMapper.selectById(groupId);

        String title = "入组申请已通过";
        String content = String.format("您申请加入小组【%s】已通过审核！", group.getName());

        send(memberId, NotificationType.GROUP_APPROVED, title, content, groupId, "GROUP");
    }
}
```

**Step 4: Commit**

```bash
git add backend/src/
git commit -m "feat: add NotificationService with business notification methods"
```

---

## Task 3: 创建通知 Controller

**Files:**
- Create: `backend/src/main/java/com/teaching/controller/NotificationController.java`

**Step 1: 创建 NotificationController**

```java
// backend/src/main/java/com/teaching/controller/NotificationController.java
package com.teaching.controller;

import com.teaching.common.Result;
import com.teaching.dto.NotificationDTO;
import com.teaching.security.UserDetailsImpl;
import com.teaching.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取通知列表
     */
    @GetMapping
    public Result<List<NotificationDTO>> getNotifications(
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal UserDetailsImpl user) {
        List<NotificationDTO> notifications = notificationService.getNotifications(user.getId(), limit);
        return Result.success(notifications);
    }

    /**
     * 获取未读通知
     */
    @GetMapping("/unread")
    public Result<List<NotificationDTO>> getUnreadNotifications(
            @AuthenticationPrincipal UserDetailsImpl user) {
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(user.getId());
        return Result.success(notifications);
    }

    /**
     * 获取未读数量
     */
    @GetMapping("/unread/count")
    public Result<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetailsImpl user) {
        Long count = notificationService.getUnreadCount(user.getId());
        return Result.success(Map.of("count", count));
    }

    /**
     * 标记为已读
     */
    @PostMapping("/{id}/read")
    public Result<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl user) {
        notificationService.markAsRead(id, user.getId());
        return Result.success();
    }

    /**
     * 标记全部为已读
     */
    @PostMapping("/read-all")
    public Result<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetailsImpl user) {
        notificationService.markAllAsRead(user.getId());
        return Result.success();
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/
git commit -m "feat: add NotificationController"
```

---

## Task 4: 集成通知到业务流程

**Files:**
- Update: `backend/src/main/java/com/teaching/service/impl/LessonServiceImpl.java`
- Update: `backend/src/main/java/com/teaching/service/impl/GradeServiceImpl.java`
- Update: `backend/src/main/java/com/teaching/service/impl/GroupServiceImpl.java`

**Step 1: 在课程创建时发送通知**

```java
// 在 LessonServiceImpl 的 createLesson 方法末尾添加
@Autowired
private NotificationService notificationService;

// 在创建课程后
notificationService.notifyLessonPublished(lesson.getId(), lesson.getSubjectId());
```

**Step 2: 在评分完成时发送通知**

```java
// 在 GradeServiceImpl 的 gradeSubmission 方法末尾添加
notificationService.notifyGradeCompleted(grade.getSubmissionId());
```

**Step 3: 在小组申请/审批时发送通知**

```java
// 在 GroupServiceImpl 的 joinGroup 方法末尾添加
notificationService.notifyGroupApplication(groupId, userId);

// 在 approveMember 方法末尾添加
notificationService.notifyGroupApproved(groupId, member.getUserId());
```

**Step 4: Commit**

```bash
git add backend/src/
git commit -m "feat: integrate notifications into business flows"
```

---

## Task 5: 创建截止提醒定时任务

**Files:**
- Create: `backend/src/main/java/com/teaching/scheduler/DeadlineReminderScheduler.java`

**Step 1: 启用定时任务（Application.java）**

```java
// 在 Application 类上添加
@EnableScheduling
```

**Step 2: 创建定时任务**

```java
// backend/src/main/java/com/teaching/scheduler/DeadlineReminderScheduler.java
package com.teaching.scheduler;

import com.teaching.entity.Lesson;
import com.teaching.mapper.LessonMapper;
import com.teaching.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineReminderScheduler {

    private final LessonMapper lessonMapper;
    private final NotificationService notificationService;

    /**
     * 每天早上9点检查作业截止提醒
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkDeadlineReminders() {
        log.info("开始检查作业截止提醒...");

        // 获取明天截止的课程
        LocalDateTime tomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime dayAfterTomorrow = tomorrow.plusDays(1);

        List<Lesson> lessons = lessonMapper.selectLessonsWithDeadlineBetween(tomorrow, dayAfterTomorrow);

        for (Lesson lesson : lessons) {
            try {
                notificationService.notifyHomeworkDeadline(lesson.getId());
                log.info("已发送截止提醒：课程ID={}, 标题={}", lesson.getId(), lesson.getTitle());
            } catch (Exception e) {
                log.error("发送截止提醒失败：课程ID={}", lesson.getId(), e);
            }
        }

        log.info("作业截止提醒检查完成，共处理 {} 个课程", lessons.size());
    }
}
```

**Step 3: 添加 Mapper 方法**

```java
// 在 LessonMapper 中添加
@Select("SELECT * FROM t_lesson WHERE deadline >= #{start} AND deadline < #{end} AND del_flag = 0")
List<Lesson> selectLessonsWithDeadlineBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
```

**Step 4: Commit**

```bash
git add backend/src/
git commit -m "feat: add deadline reminder scheduler"
```

---

## Task 6: 前端通知功能

**Files:**
- Create: `frontend/src/api/notification.ts`
- Create: `frontend/src/views/notifications/NotificationList.vue`
- Create: `frontend/src/components/NotificationBell.vue`
- Update: `frontend/src/components/Navbar.vue`

**Step 1: 创建通知 API**

```typescript
// frontend/src/api/notification.ts
import request from '@/utils/request'

export type NotificationType = 'LESSON_PUBLISHED' | 'HOMEWORK_DEADLINE' | 'GRADE_COMPLETED' | 'GROUP_APPLICATION' | 'GROUP_APPROVED'

export interface NotificationDTO {
  id: number
  userId: number
  title: string
  content: string
  type: NotificationType
  typeDescription: string
  isRead: boolean
  relatedId?: number
  relatedType?: string
  createTime: string
}

// 获取通知列表
export function getNotifications(limit = 20) {
  return request.get<NotificationDTO[]>('/notifications', { params: { limit } })
}

// 获取未读通知
export function getUnreadNotifications() {
  return request.get<NotificationDTO[]>('/notifications/unread')
}

// 获取未读数量
export function getUnreadCount() {
  return request.get<{ count: number }>('/notifications/unread/count')
}

// 标记为已读
export function markAsRead(id: number) {
  return request.post(`/notifications/${id}/read`)
}

// 标记全部为已读
export function markAllAsRead() {
  return request.post('/notifications/read-all')
}
```

**Step 2: 创建通知铃铛组件**

```vue
<!-- frontend/src/components/NotificationBell.vue -->
<template>
  <el-popover
    placement="bottom-end"
    :width="360"
    trigger="click"
    @show="loadNotifications"
  >
    <template #reference>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
        <el-button :icon="Bell" circle />
      </el-badge>
    </template>

    <div class="notification-popover">
      <div class="notification-header">
        <span>通知</span>
        <el-button v-if="unreadCount > 0" size="small" link @click="handleMarkAllRead">
          全部已读
        </el-button>
      </div>

      <el-scrollbar max-height="400px">
        <div v-if="notifications.length > 0" class="notification-list">
          <div
            v-for="notification in notifications"
            :key="notification.id"
            class="notification-item"
            :class="{ unread: !notification.isRead }"
            @click="handleClick(notification)"
          >
            <div class="notification-icon">
              <el-icon :color="getIconColor(notification.type)">
                <component :is="getIcon(notification.type)" />
              </el-icon>
            </div>
            <div class="notification-content">
              <div class="notification-title">{{ notification.title }}</div>
              <div class="notification-text">{{ notification.content }}</div>
              <div class="notification-time">{{ formatTime(notification.createTime) }}</div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无通知" :image-size="60" />
      </el-scrollbar>

      <div class="notification-footer">
        <el-button link @click="viewAll">查看全部</el-button>
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, Document, Clock, Star, UserFilled, Check } from '@element-plus/icons-vue'
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification'
import type { NotificationDTO, NotificationType } from '@/api/notification'

const router = useRouter()

const notifications = ref<NotificationDTO[]>([])
const unreadCount = ref(0)
let pollInterval: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  loadUnreadCount()
  // 每30秒轮询未读数量
  pollInterval = setInterval(loadUnreadCount, 30000)
})

onUnmounted(() => {
  if (pollInterval) {
    clearInterval(pollInterval)
  }
})

async function loadUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data.count
  } catch (error) {
    // 静默处理
  }
}

async function loadNotifications() {
  try {
    const res = await getNotifications(10)
    notifications.value = res.data
  } catch (error) {
    // 静默处理
  }
}

async function handleClick(notification: NotificationDTO) {
  if (!notification.isRead) {
    await markAsRead(notification.id)
    notification.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }

  // 根据类型跳转
  if (notification.relatedId) {
    switch (notification.relatedType) {
      case 'LESSON':
        router.push(`/lessons/${notification.relatedId}`)
        break
      case 'SUBMISSION':
        router.push(`/submissions/${notification.relatedId}`)
        break
      case 'GROUP':
        router.push(`/groups`)
        break
    }
  }
}

async function handleMarkAllRead() {
  await markAllAsRead()
  notifications.value.forEach(n => n.isRead = true)
  unreadCount.value = 0
}

function viewAll() {
  router.push('/notifications')
}

function getIcon(type: NotificationType) {
  const icons = {
    'LESSON_PUBLISHED': Document,
    'HOMEWORK_DEADLINE': Clock,
    'GRADE_COMPLETED': Star,
    'GROUP_APPLICATION': UserFilled,
    'GROUP_APPROVED': Check
  }
  return icons[type] || Bell
}

function getIconColor(type: NotificationType) {
  const colors = {
    'LESSON_PUBLISHED': '#409eff',
    'HOMEWORK_DEADLINE': '#e6a23c',
    'GRADE_COMPLETED': '#67c23a',
    'GROUP_APPLICATION': '#909399',
    'GROUP_APPROVED': '#67c23a'
  }
  return colors[type] || '#909399'
}

function formatTime(time: string) {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}
</script>

<style scoped>
.notification-popover {
  margin: -12px;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 500;
}

.notification-list {
  padding: 8px 0;
}

.notification-item {
  display: flex;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #ecf5ff;
}

.notification-icon {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 50%;
  margin-right: 12px;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.notification-text {
  font-size: 13px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.notification-footer {
  padding: 8px 16px;
  text-align: center;
  border-top: 1px solid #e4e7ed;
}
</style>
```

**Step 3: Commit**

```bash
git add frontend/src/
git commit -m "feat: add notification bell component and notification pages"
```

---

## 验证清单

1. **运行后端测试**
   ```bash
   cd backend && mvn test -q
   ```

2. **功能验证**
   - 新课程发布时，学科内学员收到通知
   - 作业评分完成时，提交者收到通知
   - 小组申请时，组长收到通知
   - 申请通过时，申请者收到通知
   - 点击通知跳转到对应页面
   - 标记已读/全部已读
   - 通知铃铛显示未读数量

下一步：继续 `02-ui-optimization.md` 完成 UI 优化。
