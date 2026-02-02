package com.teaching.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.dto.CreateLessonRequest;
import com.teaching.dto.UpdateLessonRequest;
import com.teaching.entity.Lesson;
import com.teaching.entity.NotificationType;
import com.teaching.entity.Subject;
import com.teaching.mapper.LessonMapper;
import com.teaching.mapper.SubjectMapper;
import com.teaching.mapper.SubjectStudentMapper;
import com.teaching.service.LessonService;
import com.teaching.exception.BusinessException;
import com.teaching.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 课程服务实现
 */
@Service
@RequiredArgsConstructor
public class LessonServiceImpl extends ServiceImpl<LessonMapper, Lesson> implements LessonService {

    private final LessonMapper lessonMapper;
    private final SubjectMapper subjectMapper;
    private final SubjectStudentMapper subjectStudentMapper;
    private final NotificationService notificationService;

    @Override
    public Lesson createLesson(CreateLessonRequest request) {
        Lesson lesson = request.toEntity();
        lessonMapper.insert(lesson);

        // 发送课程发布通知给所有学员
        Subject subject = subjectMapper.selectById(lesson.getSubjectId());
        List<Long> studentIds = subjectStudentMapper.selectStudentIdsBySubjectId(lesson.getSubjectId());

        if (!studentIds.isEmpty()) {
            String title = "新课程发布";
            String content = String.format("学科《%s》发布了新课程《%s》，上课时间：%s",
                    subject.getName(),
                    lesson.getTitle(),
                    lesson.getLessonTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            notificationService.createNotifications(studentIds, title, content, NotificationType.LESSON_PUBLISHED);
        }

        return lesson;
    }

    @Override
    public IPage<Lesson> listLessons(Long subjectId, int page, int size, String keyword,
                                      LocalDateTime startTime, LocalDateTime endTime) {
        Page<Lesson> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Lesson> wrapper = new LambdaQueryWrapper<>();

        if (subjectId != null) {
            wrapper.eq(Lesson::getSubjectId, subjectId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Lesson::getTitle, keyword)
                    .or().like(Lesson::getContent, keyword));
        }
        if (startTime != null) {
            wrapper.ge(Lesson::getLessonTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(Lesson::getLessonTime, endTime);
        }

        wrapper.orderByAsc(Lesson::getLessonTime);

        return lessonMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Lesson getLessonById(Long id) {
        return lessonMapper.selectById(id);
    }

    @Override
    public Lesson updateLesson(Long id, UpdateLessonRequest request) {
        Lesson lesson = lessonMapper.selectById(id);
        if (lesson == null) {
            throw new BusinessException(404, "课程不存在");
        }

        request.updateEntity(lesson);
        lessonMapper.updateById(lesson);

        return lesson;
    }

    @Override
    public void deleteLesson(Long id) {
        lessonMapper.deleteById(id);
    }

    @Override
    public List<Lesson> getLessonsBySubjectId(Long subjectId) {
        return lessonMapper.selectBySubjectId(subjectId);
    }

    @Override
    @Transactional
    public Lesson updateLessonTime(Long id, LocalDateTime lessonTime) {
        Lesson lesson = getById(id);
        if (lesson == null) {
            throw new BusinessException(404, "课程不存在");
        }

        lesson.setLessonTime(lessonTime);
        updateById(lesson);

        return lesson;
    }
}
