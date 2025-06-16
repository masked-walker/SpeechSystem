package com.example.speechsystem.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户实体")
public class User {

    @Schema(description = "用户ID", example = "1")
    private int id;

    @Schema(description = "用户名", example = "ZhangSan")
    private String username;

    @Schema(description = "用户邮箱", example = "12345@168.com")
    private String email;

    @Schema(description = "用户密码（加密后）", example = "hashed_password")
    private String password;

    @Schema(description = "用户创建时间", example = "2025-06-16T14:25:00")
    private LocalDateTime createdAt;
}
