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

        if (metaObject.hasSetter("createTime") && metaObject.getValue("createTime") == null) {
            this.setFieldValByName("createTime", now, metaObject);
        }
        if (metaObject.hasSetter("updateTime") && metaObject.getValue("updateTime") == null) {
            this.setFieldValByName("updateTime", now, metaObject);
        }
        if (metaObject.hasSetter("createBy")) {
            Object value = metaObject.getValue("createBy");
            if (value == null || "".equals(value)) {
                this.setFieldValByName("createBy", currentUser, metaObject);
            }
        }
        if (metaObject.hasSetter("updateBy")) {
            Object value = metaObject.getValue("updateBy");
            if (value == null || "".equals(value)) {
                this.setFieldValByName("updateBy", currentUser, metaObject);
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.hasSetter("updateTime")) {
            this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        }
        if (metaObject.hasSetter("updateBy")) {
            this.setFieldValByName("updateBy", getCurrentUsername(), metaObject);
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}
