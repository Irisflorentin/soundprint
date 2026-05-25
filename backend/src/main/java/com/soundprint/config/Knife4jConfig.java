package com.soundprint.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j (Swagger) API 文档配置
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI soundprintOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Soundprint API")
                        .description("个人无损音乐库 + 在线播放器 + 音频格式转换工坊")
                        .version("1.0.0"));
    }
}
