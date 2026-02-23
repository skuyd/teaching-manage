package com.teaching.service;

import com.teaching.dto.UserImportResultDTO;
import com.teaching.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

public interface UserExcelService {

    /**
     * 导出用户列表到 Excel
     */
    ByteArrayOutputStream exportUsers(List<User> users);

    /**
     * 生成导入模板
     */
    ByteArrayOutputStream generateTemplate();

    /**
     * 解析并导入用户
     */
    UserImportResultDTO importUsers(MultipartFile file);
}
