package com.soundprint.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 上传校验配置
 * 绑定 application.yml 里 soundprint.upload.* 的配置项
 * 逗号分隔的字符串（如 flac,mp3,wav）会被 Spring 自动绑定成 List
 */
@Data
@Component
@ConfigurationProperties(prefix = "soundprint.upload")
public class UploadProperties {
    private String maxFileSize;
    private List<String> allowedAudioFormats;
    private List<String> allowedImageFormats;
}
