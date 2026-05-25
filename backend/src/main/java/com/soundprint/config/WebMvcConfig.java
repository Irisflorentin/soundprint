package com.soundprint.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：把本地存储目录暴露为 /files/** 静态资源。
 *
 * 数据库里只保存 cover/xxx.jpg 这样的相对路径，前端访问时拼成
 * /files/cover/xxx.jpg，最终由这里映射到 D:/soundprint-storage/cover/xxx.jpg。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final StorageProperties storageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String baseDir = storageProperties.getBaseDir();
        if (!baseDir.endsWith("/") && !baseDir.endsWith("\\")) {
            baseDir = baseDir + "/";
        }
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + baseDir)
                .setCachePeriod(3600);
    }
}
