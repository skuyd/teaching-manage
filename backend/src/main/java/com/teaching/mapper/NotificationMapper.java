package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 通知Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 查询用户未读通知数量
     */
    @Select("SELECT COUNT(*) FROM t_notification WHERE user_id = #{userId} AND is_read = 0 AND del_flag = 0")
    Integer countUnreadByUserId(Long userId);
}
