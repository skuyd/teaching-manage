package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teaching.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_notification")
public class Notification extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型
     */
    private NotificationType type;

    /**
     * 是否已读 (0未读 1已读)
     */
    private Boolean isRead;
}
