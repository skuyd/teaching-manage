# 阶段一：后端基础框架搭建

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 搭建 Spring Boot 3 后端项目，配置 SQLite 数据库，实现用户实体和基础架构。

**Architecture:** 采用分层架构（Controller-Service-Mapper），使用 MyBatis-Plus 简化数据访问，JWT 实现无状态认证。

**Tech Stack:** Java 17, Spring Boot 3.2, MyBatis-Plus 3.5, SQLite, JWT, JUnit 5, Mockito

---

## Task 1: 初始化 Spring Boot 项目

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/teaching/Application.java`
- Create: `backend/src/main/resources/application.yml`

**Step 1: 创建项目目录结构**

```bash
mkdir -p backend/src/main/java/com/teaching
mkdir -p backend/src/main/resources
mkdir -p backend/src/test/java/com/teaching
```

**Step 2: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.teaching</groupId>
    <artifactId>teaching-manage</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>Teaching Management System</name>
    <description>教学管理系统后端服务</description>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>17</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <jjwt.version>0.12.3</jjwt.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- SQLite -->
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.44.1.0</version>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**Step 3: 创建主应用类**

```java
// backend/src/main/java/com/teaching/Application.java
package com.teaching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**Step 4: 创建配置文件**

```yaml
# backend/src/main/resources/application.yml
server:
  port: 8080

spring:
  application:
    name: teaching-manage
  datasource:
    driver-class-name: org.sqlite.JDBC
    url: jdbc:sqlite:./data/teaching.db
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: delFlag
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: teaching-manage-secret-key-must-be-at-least-256-bits-long
  expiration: 86400000  # 24 hours in milliseconds

file:
  upload-dir: ./uploads
```

**Step 5: 验证项目结构**

```bash
cd backend && ls -la src/main/java/com/teaching/
```

Expected: 显示 Application.java 文件

**Step 6: Commit**

```bash
git add backend/
git commit -m "chore: initialize Spring Boot 3 backend project"
```

---

## Task 2: 创建基础实体类

**Files:**
- Create: `backend/src/main/java/com/teaching/common/BaseEntity.java`
- Create: `backend/src/main/java/com/teaching/entity/User.java`
- Create: `backend/src/main/java/com/teaching/enums/UserRole.java`
- Test: `backend/src/test/java/com/teaching/entity/UserTest.java`

**Step 1: 编写 User 实体测试（红灯）**

```java
// backend/src/test/java/com/teaching/entity/UserTest.java
package com.teaching.entity;

import com.teaching.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("创建用户实体应包含所有必需字段")
    void createUser_shouldHaveAllRequiredFields() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encoded_password");
        user.setRole(UserRole.STUDENT);
        user.setName("测试用户");
        user.setEmail("test@example.com");

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("encoded_password", user.getPassword());
        assertEquals(UserRole.STUDENT, user.getRole());
        assertEquals("测试用户", user.getName());
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    @DisplayName("用户角色枚举应包含三种角色")
    void userRole_shouldHaveThreeRoles() {
        assertEquals(3, UserRole.values().length);
        assertNotNull(UserRole.ADMIN);
        assertNotNull(UserRole.TEACHER);
        assertNotNull(UserRole.STUDENT);
    }

    @Test
    @DisplayName("新用户默认删除标志应为0")
    void newUser_shouldHaveDefaultDelFlag() {
        User user = new User();
        assertEquals(0, user.getDelFlag());
    }

    @Test
    @DisplayName("新用户默认版本号应为0")
    void newUser_shouldHaveDefaultVersion() {
        User user = new User();
        assertEquals(0, user.getVersion());
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=UserTest -q
```

Expected: FAIL - User 类和 UserRole 枚举不存在

**Step 3: 创建 UserRole 枚举**

```java
// backend/src/main/java/com/teaching/enums/UserRole.java
package com.teaching.enums;

public enum UserRole {
    ADMIN,
    TEACHER,
    STUDENT
}
```

**Step 4: 创建 BaseEntity 基类**

```java
// backend/src/main/java/com/teaching/common/BaseEntity.java
package com.teaching.common;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Version
    private Integer version = 0;

    @TableLogic
    private Integer delFlag = 0;

    @TableField(fill = FieldFill.INSERT)
    private String createBy = "";

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy = "";

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

**Step 5: 创建 User 实体**

```java
// backend/src/main/java/com/teaching/entity/User.java
package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teaching.common.BaseEntity;
import com.teaching.enums.UserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class User extends BaseEntity {

    private String username;

    private String password;

    private UserRole role;

    private String name;

    private String email;

    private String avatar;
}
```

**Step 6: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=UserTest -q
```

Expected: PASS - 4 tests passed

**Step 7: Commit**

```bash
git add backend/src/
git commit -m "feat: add User entity and UserRole enum with TDD"
```

---

## Task 3: 创建数据库初始化脚本

**Files:**
- Create: `backend/src/main/resources/schema.sql`
- Create: `backend/data/.gitkeep`

**Step 1: 创建数据目录**

```bash
mkdir -p backend/data
touch backend/data/.gitkeep
echo "*.db" > backend/data/.gitignore
```

**Step 2: 创建 schema.sql**

```sql
-- backend/src/main/resources/schema.sql

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) DEFAULT '',
    avatar VARCHAR(200) DEFAULT '',
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 学科表
CREATE TABLE IF NOT EXISTS t_subject (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT DEFAULT '',
    is_grouped TINYINT NOT NULL DEFAULT 0,
    min_members INT NOT NULL DEFAULT 1,
    max_members INT NOT NULL DEFAULT 1,
    start_date DATE,
    end_date DATE,
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 课程表
CREATE TABLE IF NOT EXISTS t_lesson (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT DEFAULT '',
    lesson_time DATETIME NOT NULL,
    homework_desc TEXT DEFAULT '',
    submit_type VARCHAR(20) NOT NULL DEFAULT 'PERSONAL',
    deadline DATETIME,
    allow_late TINYINT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 小组表
CREATE TABLE IF NOT EXISTS t_group (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    leader_id INTEGER NOT NULL,
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 小组成员表
CREATE TABLE IF NOT EXISTS t_group_member (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    group_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 作业提交表
CREATE TABLE IF NOT EXISTS t_submission (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    lesson_id INTEGER NOT NULL,
    submitter_id INTEGER NOT NULL,
    group_id INTEGER,
    file_path VARCHAR(500) NOT NULL,
    submit_time DATETIME NOT NULL,
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 评分表
CREATE TABLE IF NOT EXISTS t_grade (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    submission_id INTEGER NOT NULL,
    grade VARCHAR(10) NOT NULL,
    comment TEXT DEFAULT '',
    grader_id INTEGER NOT NULL,
    grade_time DATETIME NOT NULL,
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 代码评论表
CREATE TABLE IF NOT EXISTS t_code_comment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    submission_id INTEGER NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    line_number INT NOT NULL,
    content TEXT NOT NULL,
    commenter_id INTEGER NOT NULL,
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 通知表
CREATE TABLE IF NOT EXISTS t_notification (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT DEFAULT '',
    type VARCHAR(50) NOT NULL,
    is_read TINYINT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 学员-学科关联表
CREATE TABLE IF NOT EXISTS t_subject_student (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_user_username ON t_user(username);
CREATE INDEX IF NOT EXISTS idx_lesson_subject_id ON t_lesson(subject_id);
CREATE INDEX IF NOT EXISTS idx_group_subject_id ON t_group(subject_id);
CREATE INDEX IF NOT EXISTS idx_group_member_group_id ON t_group_member(group_id);
CREATE INDEX IF NOT EXISTS idx_group_member_user_id ON t_group_member(user_id);
CREATE INDEX IF NOT EXISTS idx_submission_lesson_id ON t_submission(lesson_id);
CREATE INDEX IF NOT EXISTS idx_grade_submission_id ON t_grade(submission_id);
CREATE INDEX IF NOT EXISTS idx_notification_user_id ON t_notification(user_id);
CREATE INDEX IF NOT EXISTS idx_subject_student_subject_id ON t_subject_student(subject_id);
CREATE INDEX IF NOT EXISTS idx_subject_student_student_id ON t_subject_student(student_id);
```

**Step 3: Commit**

```bash
git add backend/src/main/resources/schema.sql backend/data/
git commit -m "feat: add SQLite database schema with all tables"
```

---

## Task 4: 创建 UserMapper 和 UserService

**Files:**
- Create: `backend/src/main/java/com/teaching/mapper/UserMapper.java`
- Create: `backend/src/main/java/com/teaching/service/UserService.java`
- Create: `backend/src/main/java/com/teaching/service/impl/UserServiceImpl.java`
- Test: `backend/src/test/java/com/teaching/service/UserServiceTest.java`

**Step 1: 编写 UserService 测试（红灯）**

```java
// backend/src/test/java/com/teaching/service/UserServiceTest.java
package com.teaching.service;

import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.mapper.UserMapper;
import com.teaching.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("根据用户名查询用户")
    void findByUsername_shouldReturnUser() {
        User expected = new User();
        expected.setId(1L);
        expected.setUsername("testuser");
        expected.setRole(UserRole.STUDENT);

        when(userMapper.selectByUsername("testuser")).thenReturn(expected);

        User result = userService.findByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userMapper).selectByUsername("testuser");
    }

    @Test
    @DisplayName("用户名不存在时返回null")
    void findByUsername_shouldReturnNull_whenNotFound() {
        when(userMapper.selectByUsername("nonexistent")).thenReturn(null);

        User result = userService.findByUsername("nonexistent");

        assertNull(result);
    }

    @Test
    @DisplayName("创建用户时密码应被加密")
    void createUser_shouldEncodePassword() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("plainPassword");
        user.setRole(UserRole.STUDENT);
        user.setName("新用户");

        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        userService.createUser(user);

        assertEquals("encodedPassword", user.getPassword());
        verify(passwordEncoder).encode("plainPassword");
        verify(userMapper).insert(user);
    }

    @Test
    @DisplayName("检查用户名是否存在")
    void existsByUsername_shouldReturnTrue_whenExists() {
        when(userMapper.countByUsername("existinguser")).thenReturn(1L);

        boolean result = userService.existsByUsername("existinguser");

        assertTrue(result);
    }

    @Test
    @DisplayName("检查用户名不存在")
    void existsByUsername_shouldReturnFalse_whenNotExists() {
        when(userMapper.countByUsername("newuser")).thenReturn(0L);

        boolean result = userService.existsByUsername("newuser");

        assertFalse(result);
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=UserServiceTest -q
```

Expected: FAIL - UserService 和 UserMapper 不存在

**Step 3: 创建 UserMapper**

```java
// backend/src/main/java/com/teaching/mapper/UserMapper.java
package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM t_user WHERE username = #{username} AND del_flag = 0")
    User selectByUsername(String username);

    @Select("SELECT COUNT(*) FROM t_user WHERE username = #{username} AND del_flag = 0")
    Long countByUsername(String username);
}
```

**Step 4: 创建 UserService 接口**

```java
// backend/src/main/java/com/teaching/service/UserService.java
package com.teaching.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teaching.entity.User;

public interface UserService extends IService<User> {

    User findByUsername(String username);

    void createUser(User user);

    boolean existsByUsername(String username);
}
```

**Step 5: 创建 UserServiceImpl**

```java
// backend/src/main/java/com/teaching/service/impl/UserServiceImpl.java
package com.teaching.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.entity.User;
import com.teaching.mapper.UserMapper;
import com.teaching.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public void createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.countByUsername(username) > 0;
    }
}
```

**Step 6: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=UserServiceTest -q
```

Expected: PASS - 5 tests passed

**Step 7: Commit**

```bash
git add backend/src/
git commit -m "feat: add UserMapper and UserService with TDD"
```

---

## Task 5: 配置 MyBatis-Plus 自动填充

**Files:**
- Create: `backend/src/main/java/com/teaching/config/MybatisPlusConfig.java`
- Create: `backend/src/main/java/com/teaching/config/MetaObjectHandlerConfig.java`
- Test: `backend/src/test/java/com/teaching/config/MetaObjectHandlerConfigTest.java`

**Step 1: 编写 MetaObjectHandler 测试（红灯）**

```java
// backend/src/test/java/com/teaching/config/MetaObjectHandlerConfigTest.java
package com.teaching.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetaObjectHandlerConfigTest {

    @Mock
    private MetaObject metaObject;

    @Test
    @DisplayName("插入时应填充createTime和updateTime")
    void insertFill_shouldFillCreateTimeAndUpdateTime() {
        MetaObjectHandlerConfig handler = new MetaObjectHandlerConfig();

        when(metaObject.hasSetter("createTime")).thenReturn(true);
        when(metaObject.hasSetter("updateTime")).thenReturn(true);
        when(metaObject.hasSetter("createBy")).thenReturn(true);
        when(metaObject.hasSetter("updateBy")).thenReturn(true);

        handler.insertFill(metaObject);

        verify(metaObject, atLeastOnce()).hasSetter("createTime");
        verify(metaObject, atLeastOnce()).hasSetter("updateTime");
    }

    @Test
    @DisplayName("更新时应填充updateTime")
    void updateFill_shouldFillUpdateTime() {
        MetaObjectHandlerConfig handler = new MetaObjectHandlerConfig();

        when(metaObject.hasSetter("updateTime")).thenReturn(true);
        when(metaObject.hasSetter("updateBy")).thenReturn(true);

        handler.updateFill(metaObject);

        verify(metaObject, atLeastOnce()).hasSetter("updateTime");
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=MetaObjectHandlerConfigTest -q
```

Expected: FAIL - MetaObjectHandlerConfig 不存在

**Step 3: 创建 MybatisPlusConfig**

```java
// backend/src/main/java/com/teaching/config/MybatisPlusConfig.java
package com.teaching.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.teaching.mapper")
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.SQLITE));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
```

**Step 4: 创建 MetaObjectHandlerConfig**

```java
// backend/src/main/java/com/teaching/config/MetaObjectHandlerConfig.java
package com.teaching.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MetaObjectHandlerConfig implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String currentUser = getCurrentUsername();

        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createBy", String.class, currentUser);
        this.strictInsertFill(metaObject, "updateBy", String.class, currentUser);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUsername());
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}
```

**Step 5: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=MetaObjectHandlerConfigTest -q
```

Expected: PASS - 2 tests passed

**Step 6: Commit**

```bash
git add backend/src/
git commit -m "feat: add MyBatis-Plus configuration with auto-fill"
```

---

## Task 6: 创建统一响应结构

**Files:**
- Create: `backend/src/main/java/com/teaching/common/Result.java`
- Create: `backend/src/main/java/com/teaching/common/ResultCode.java`
- Test: `backend/src/test/java/com/teaching/common/ResultTest.java`

**Step 1: 编写 Result 测试（红灯）**

```java
// backend/src/test/java/com/teaching/common/ResultTest.java
package com.teaching.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    @DisplayName("成功响应应返回正确的code和message")
    void success_shouldReturnCorrectCodeAndMessage() {
        Result<String> result = Result.success("test data");

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("test data", result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("无数据成功响应")
    void success_shouldWorkWithoutData() {
        Result<Void> result = Result.success();

        assertEquals(200, result.getCode());
        assertNull(result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("失败响应应返回正确的code和message")
    void error_shouldReturnCorrectCodeAndMessage() {
        Result<Void> result = Result.error(400, "参数错误");

        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
        assertNull(result.getData());
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("使用ResultCode创建响应")
    void error_shouldWorkWithResultCode() {
        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED);

        assertEquals(401, result.getCode());
        assertEquals("未授权", result.getMessage());
        assertFalse(result.isSuccess());
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=ResultTest -q
```

Expected: FAIL - Result 和 ResultCode 不存在

**Step 3: 创建 ResultCode 枚举**

```java
// backend/src/main/java/com/teaching/common/ResultCode.java
package com.teaching.common;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误码
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    TOKEN_EXPIRED(1004, "Token已过期"),
    TOKEN_INVALID(1005, "Token无效");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

**Step 4: 创建 Result 类**

```java
// backend/src/main/java/com/teaching/common/Result.java
package com.teaching.common;

import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    private Result() {}

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = success();
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return error(resultCode.getCode(), resultCode.getMessage());
    }

    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }
}
```

**Step 5: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=ResultTest -q
```

Expected: PASS - 4 tests passed

**Step 6: Commit**

```bash
git add backend/src/
git commit -m "feat: add unified Result response structure"
```

---

## Task 7: 实现全局异常处理

**Files:**
- Create: `backend/src/main/java/com/teaching/exception/BusinessException.java`
- Create: `backend/src/main/java/com/teaching/exception/GlobalExceptionHandler.java`
- Test: `backend/src/test/java/com/teaching/exception/GlobalExceptionHandlerTest.java`

**Step 1: 编写异常处理测试（红灯）**

```java
// backend/src/test/java/com/teaching/exception/GlobalExceptionHandlerTest.java
package com.teaching.exception;

import com.teaching.common.Result;
import com.teaching.common.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("处理业务异常")
    void handleBusinessException_shouldReturnCorrectResult() {
        BusinessException ex = new BusinessException(ResultCode.USER_NOT_FOUND);

        Result<Void> result = handler.handleBusinessException(ex);

        assertEquals(1001, result.getCode());
        assertEquals("用户不存在", result.getMessage());
    }

    @Test
    @DisplayName("处理自定义消息的业务异常")
    void handleBusinessException_shouldReturnCustomMessage() {
        BusinessException ex = new BusinessException(400, "自定义错误消息");

        Result<Void> result = handler.handleBusinessException(ex);

        assertEquals(400, result.getCode());
        assertEquals("自定义错误消息", result.getMessage());
    }

    @Test
    @DisplayName("处理参数校验异常")
    void handleValidationException_shouldReturnFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("user", "username", "用户名不能为空");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        Result<Void> result = handler.handleValidationException(ex);

        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("用户名不能为空"));
    }

    @Test
    @DisplayName("处理通用异常")
    void handleException_shouldReturnInternalError() {
        Exception ex = new RuntimeException("未知错误");

        Result<Void> result = handler.handleException(ex);

        assertEquals(500, result.getCode());
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=GlobalExceptionHandlerTest -q
```

Expected: FAIL - BusinessException 和 GlobalExceptionHandler 不存在

**Step 3: 创建 BusinessException**

```java
// backend/src/main/java/com/teaching/exception/BusinessException.java
package com.teaching.exception;

import com.teaching.common.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final String message;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMessage());
    }
}
```

**Step 4: 创建 GlobalExceptionHandler**

```java
// backend/src/main/java/com/teaching/exception/GlobalExceptionHandler.java
package com.teaching.exception;

import com.teaching.common.Result;
import com.teaching.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: code={}, message={}", ex.getCode(), ex.getMessage());
        return Result.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("系统异常", ex);
        return Result.error(ResultCode.INTERNAL_ERROR);
    }
}
```

**Step 5: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=GlobalExceptionHandlerTest -q
```

Expected: PASS - 4 tests passed

**Step 6: Commit**

```bash
git add backend/src/
git commit -m "feat: add global exception handler"
```

---

## 验证清单

完成 Task 1-7 后，验证：

1. **项目结构完整**
   ```bash
   tree backend/src/main/java/com/teaching/
   ```

2. **所有测试通过**
   ```bash
   cd backend && mvn test -q
   ```

3. **应用可启动**（需先完成 Security 配置）
   ```bash
   cd backend && mvn spring-boot:run
   ```

下一步：继续 `02-jwt-auth.md` 完成 JWT 认证实现。
