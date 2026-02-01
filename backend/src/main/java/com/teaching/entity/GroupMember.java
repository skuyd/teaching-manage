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
