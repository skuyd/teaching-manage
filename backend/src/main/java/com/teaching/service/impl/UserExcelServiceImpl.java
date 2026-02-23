package com.teaching.service.impl;

import com.teaching.dto.UserImportResultDTO;
import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.service.UserExcelService;
import com.teaching.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserExcelServiceImpl implements UserExcelService {

    private final UserService userService;

    private static final String[] EXPORT_HEADERS = {"用户名", "姓名", "角色", "邮箱", "创建时间"};
    private static final String[] TEMPLATE_HEADERS = {"用户名(必填)", "姓名(必填)", "密码(必填)", "角色(必填:ADMIN/TEACHER/STUDENT)", "邮箱(选填)"};
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ByteArrayOutputStream exportUsers(List<User> users) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("用户列表");

            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(EXPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getUsername());
                row.createCell(1).setCellValue(user.getName());
                row.createCell(2).setCellValue(getRoleLabel(user.getRole()));
                row.createCell(3).setCellValue(user.getEmail() != null ? user.getEmail() : "");
                row.createCell(4).setCellValue(user.getCreateTime() != null ?
                        user.getCreateTime().format(DATE_FORMATTER) : "");
            }

            // 自动调整列宽
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out;
        } catch (IOException e) {
            log.error("导出用户 Excel 失败", e);
            throw new RuntimeException("导出失败", e);
        }
    }

    @Override
    public ByteArrayOutputStream generateTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("用户导入模板");

            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(TEMPLATE_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // 创建示例数据
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("zhangsan");
            exampleRow.createCell(1).setCellValue("张三");
            exampleRow.createCell(2).setCellValue("123456");
            exampleRow.createCell(3).setCellValue("STUDENT");
            exampleRow.createCell(4).setCellValue("zhangsan@example.com");

            // 自动调整列宽
            for (int i = 0; i < TEMPLATE_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out;
        } catch (IOException e) {
            log.error("生成导入模板失败", e);
            throw new RuntimeException("生成模板失败", e);
        }
    }

    @Override
    public UserImportResultDTO importUsers(MultipartFile file) {
        UserImportResultDTO result = new UserImportResultDTO();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            result.setTotalCount(lastRowNum); // 不含表头

            // 从第2行开始读取（跳过表头）
            for (int rowNum = 1; rowNum <= lastRowNum; rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                try {
                    User user = parseUserFromRow(row, rowNum + 1, result);
                    if (user != null) {
                        userService.createUser(user);
                        result.incrementSuccess();
                    }
                } catch (Exception e) {
                    log.warn("导入第{}行失败: {}", rowNum + 1, e.getMessage());
                    result.addError("第" + (rowNum + 1) + "行: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("解析 Excel 文件失败", e);
            result.addError("文件解析失败: " + e.getMessage());
        }

        return result;
    }

    private User parseUserFromRow(Row row, int rowNumber, UserImportResultDTO result) {
        String username = getCellStringValue(row.getCell(0));
        String name = getCellStringValue(row.getCell(1));
        String password = getCellStringValue(row.getCell(2));
        String roleStr = getCellStringValue(row.getCell(3));
        String email = getCellStringValue(row.getCell(4));

        // 验证必填字段
        if (!StringUtils.hasText(username)) {
            result.addError("第" + rowNumber + "行: 用户名不能为空");
            return null;
        }
        if (username.length() < 3 || username.length() > 50) {
            result.addError("第" + rowNumber + "行: 用户名长度应为3-50个字符");
            return null;
        }
        if (!StringUtils.hasText(name)) {
            result.addError("第" + rowNumber + "行: 姓名不能为空");
            return null;
        }
        if (!StringUtils.hasText(password)) {
            result.addError("第" + rowNumber + "行: 密码不能为空");
            return null;
        }
        if (password.length() < 6) {
            result.addError("第" + rowNumber + "行: 密码长度至少6个字符");
            return null;
        }
        if (!StringUtils.hasText(roleStr)) {
            result.addError("第" + rowNumber + "行: 角色不能为空");
            return null;
        }

        // 验证角色
        UserRole role;
        try {
            role = UserRole.valueOf(roleStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            result.addError("第" + rowNumber + "行: 无效的角色值，应为ADMIN/TEACHER/STUDENT");
            return null;
        }

        // 验证邮箱格式（如果填写了）
        if (StringUtils.hasText(email) && !EMAIL_PATTERN.matcher(email).matches()) {
            result.addError("第" + rowNumber + "行: 邮箱格式不正确");
            return null;
        }

        // 检查用户名是否已存在
        if (userService.existsByUsername(username)) {
            result.addError("第" + rowNumber + "行: 用户名'" + username + "'已存在");
            return null;
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setName(name.trim());
        user.setPassword(password);
        user.setRole(role);
        user.setEmail(StringUtils.hasText(email) ? email.trim() : null);

        return user;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < 5; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && StringUtils.hasText(getCellStringValue(cell))) {
                return false;
            }
        }
        return true;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private String getRoleLabel(UserRole role) {
        return switch (role) {
            case ADMIN -> "管理员";
            case TEACHER -> "教员";
            case STUDENT -> "学员";
        };
    }
}
