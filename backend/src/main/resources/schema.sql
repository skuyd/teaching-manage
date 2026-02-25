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
    theme VARCHAR(20) NOT NULL DEFAULT 'tech-blue',
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
CREATE INDEX IF NOT EXISTS idx_code_comment_submission_id ON t_code_comment(submission_id);
CREATE INDEX IF NOT EXISTS idx_code_comment_file_path ON t_code_comment(submission_id, file_path);
CREATE INDEX IF NOT EXISTS idx_notification_user_id ON t_notification(user_id);
CREATE INDEX IF NOT EXISTS idx_subject_student_subject_id ON t_subject_student(subject_id);
CREATE INDEX IF NOT EXISTS idx_subject_student_student_id ON t_subject_student(student_id);
