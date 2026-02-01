package com.teaching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.dto.CreateGroupRequest;
import com.teaching.entity.Group;
import com.teaching.entity.GroupMember;
import com.teaching.entity.Subject;
import com.teaching.enums.MemberStatus;
import com.teaching.mapper.GroupMapper;
import com.teaching.mapper.GroupMemberMapper;
import com.teaching.mapper.SubjectMapper;
import com.teaching.service.GroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GroupService groupService;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    private Subject testSubject;
    private Group testGroup;

    @BeforeEach
    void setUp() {
        testSubject = new Subject();
        testSubject.setName("测试学科");
        testSubject.setDescription("测试描述");
        testSubject.setIsGrouped(true);
        testSubject.setMinMembers(3);
        testSubject.setMaxMembers(5);
        testSubject.setStartDate(LocalDate.now());
        testSubject.setEndDate(LocalDate.now().plusMonths(3));
        subjectMapper.insert(testSubject);

        testGroup = new Group();
        testGroup.setSubjectId(testSubject.getId());
        testGroup.setName("测试小组");
        testGroup.setLeaderId(2L);
        groupMapper.insert(testGroup);

        GroupMember leader = new GroupMember();
        leader.setGroupId(testGroup.getId());
        leader.setUserId(2L);
        leader.setStatus(MemberStatus.APPROVED);
        groupMemberMapper.insert(leader);
    }

    @Test
    @DisplayName("学员创建小组成功")
    @WithMockUser(username = "1", roles = {"STUDENT"})
    void createGroup_asStudent_shouldSuccess() throws Exception {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setSubjectId(testSubject.getId());
        request.setName("新建小组");

        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("获取学科的小组列表")
    @WithMockUser(username = "1", roles = {"STUDENT"})
    void listGroupsBySubject_shouldSuccess() throws Exception {
        mockMvc.perform(get("/api/groups/subject/" + testSubject.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("获取我在某学科的小组")
    @WithMockUser(username = "2", roles = {"STUDENT"})
    void getMyGroup_shouldSuccess() throws Exception {
        mockMvc.perform(get("/api/groups/my/" + testSubject.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("申请加入小组")
    @WithMockUser(username = "1", roles = {"STUDENT"})
    void joinGroup_shouldSuccess() throws Exception {
        mockMvc.perform(post("/api/groups/" + testGroup.getId() + "/join"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("退出小组")
    @WithMockUser(username = "1", roles = {"STUDENT"})
    void leaveGroup_shouldSuccess() throws Exception {
        GroupMember member = new GroupMember();
        member.setGroupId(testGroup.getId());
        member.setUserId(1L);
        member.setStatus(MemberStatus.APPROVED);
        groupMemberMapper.insert(member);

        mockMvc.perform(post("/api/groups/" + testGroup.getId() + "/leave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("组长批准成员")
    @WithMockUser(username = "2", roles = {"STUDENT"})
    void approveMember_shouldSuccess() throws Exception {
        GroupMember pendingMember = new GroupMember();
        pendingMember.setGroupId(testGroup.getId());
        pendingMember.setUserId(3L);
        pendingMember.setStatus(MemberStatus.PENDING);
        groupMemberMapper.insert(pendingMember);

        mockMvc.perform(post("/api/groups/" + testGroup.getId() + "/members/" + pendingMember.getId() + "/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("组长拒绝成员")
    @WithMockUser(username = "2", roles = {"STUDENT"})
    void rejectMember_shouldSuccess() throws Exception {
        GroupMember pendingMember = new GroupMember();
        pendingMember.setGroupId(testGroup.getId());
        pendingMember.setUserId(4L);
        pendingMember.setStatus(MemberStatus.PENDING);
        groupMemberMapper.insert(pendingMember);

        mockMvc.perform(post("/api/groups/" + testGroup.getId() + "/members/" + pendingMember.getId() + "/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("组长移除成员")
    @WithMockUser(username = "2", roles = {"STUDENT"})
    void removeMember_shouldSuccess() throws Exception {
        GroupMember member = new GroupMember();
        member.setGroupId(testGroup.getId());
        member.setUserId(5L);
        member.setStatus(MemberStatus.APPROVED);
        groupMemberMapper.insert(member);

        mockMvc.perform(delete("/api/groups/" + testGroup.getId() + "/members/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("组长解散小组")
    @WithMockUser(username = "2", roles = {"STUDENT"})
    void disbandGroup_shouldSuccess() throws Exception {
        mockMvc.perform(delete("/api/groups/" + testGroup.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
