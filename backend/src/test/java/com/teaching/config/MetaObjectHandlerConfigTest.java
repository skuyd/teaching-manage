package com.teaching.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.teaching.entity.User;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MetaObjectHandlerConfigTest {

    @Test
    @DisplayName("插入时应填充createTime和updateTime")
    void insertFill_shouldFillCreateTimeAndUpdateTime() {
        MetaObjectHandlerConfig handler = new MetaObjectHandlerConfig();

        User user = new User();
        MetaObject metaObject = SystemMetaObject.forObject(user);

        handler.insertFill(metaObject);

        assertThat(user.getCreateTime()).isNotNull();
        assertThat(user.getUpdateTime()).isNotNull();
        assertThat(user.getCreateBy()).isNotNull();
        assertThat(user.getUpdateBy()).isNotNull();
    }

    @Test
    @DisplayName("更新时应填充updateTime")
    void updateFill_shouldFillUpdateTime() {
        MetaObjectHandlerConfig handler = new MetaObjectHandlerConfig();

        User user = new User();
        MetaObject metaObject = SystemMetaObject.forObject(user);

        handler.updateFill(metaObject);

        assertThat(user.getUpdateTime()).isNotNull();
        assertThat(user.getUpdateBy()).isNotNull();
        assertThat(user.getUpdateBy()).isEqualTo("system");
    }
}
