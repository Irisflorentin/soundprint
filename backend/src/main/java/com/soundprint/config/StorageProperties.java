package com.soundprint.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 存储路径配置
 * 绑定 application.yml 里 soundprint.storage.* 的配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "soundprint.storage")
public class StorageProperties {
    private String baseDir;
    private String audioDir;
    private String coverDir;
    private String avatarDir;
    private String conversionDir;
    private String tempDir;
}
