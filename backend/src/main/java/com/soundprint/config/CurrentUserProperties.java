package com.soundprint.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 当前用户配置（临时方案）
 * 绑定 application.yml 里 soundprint.current-user-id。
 * Phase 4 加登录后，改成从 Spring Security Context 取，业务代码无需变动。
 */
@Data
@Component
@ConfigurationProperties(prefix = "soundprint")
public class CurrentUserProperties {
    /** soundprint.current-user-id（relaxed binding 自动映射 current-user-id → currentUserId） */
    private Long currentUserId;
}
