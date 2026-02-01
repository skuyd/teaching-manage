package com.teaching.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.dto.CreateLessonRequest;
import com.teaching.dto.UpdateLessonRequest;
import com.teaching.entity.Lesson;
import com.teaching.mapper.LessonMapper;
import com.teaching.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程服务实现
 */
@Service
@RequiredArgsConstructor
public class LessonServiceImpl extends ServiceImpl<LessonMapper, Lesson> implements LessonService {

    private final LessonMapper lessonMapper;

    @Override
    public Lesson createLesson(CreateLessonRequest request) {
        Lesson lesson = request.toEntity();
        lessonMapper.insert(lesson);
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
            throw new RuntimeException("课程不存在");
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
}
