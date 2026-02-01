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
