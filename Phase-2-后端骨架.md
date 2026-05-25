# Phase 2：后端骨架与第一个 API

> Soundprint 项目的第三个阶段文档。
> 把整份文档完整复制粘贴给 Claude Code，作为它的任务指令。
> 本阶段产出后，后端能跑起来、能连数据库、能通过浏览器/Postman 看到种子数据。

---

## 🎯 阶段目标

1. 验证 Maven 3.9.16 已就位（用户已手动升级）
2. 在 `D:\Claude_Playground\Soundprint\backend\` 建立 Spring Boot 3.2 项目
3. 配置 `application.yml` 连接 MySQL 8.0（端口 3307）
4. 建立分层包结构 + 基础设施类（统一响应、全局异常、配置类）
5. 用 MyBatis-Plus Generator 为 11 张表一次性生成 Entity + Mapper
6. 集成 Knife4j（API 文档可视化）
7. 实现两个端到端打通的 API：
   - `GET /api/health` —— 健康检查
   - `GET /api/tracks?page=1&size=10` —— 分页查询曲目（含艺术家、专辑名）
8. 在浏览器/Knife4j 上验证 API 返回种子数据
9. commit + push 到 GitHub

**重要**：本阶段只搭后端骨架 + 第一个 API，不写完整业务逻辑（那是 Phase 3）、不动前端。

---

## 📋 任务清单

### 任务 1：环境核验

执行：

```powershell
Write-Output "=== Maven ==="
mvn -version
Write-Output "=== Java ==="
java -version
Write-Output "=== JAVA_HOME ==="
$env:JAVA_HOME
Write-Output "=== MAVEN_HOME ==="
$env:MAVEN_HOME
```

**判断标准**：
- Maven ≥ 3.9.x（用户应已升级到 3.9.16）
- Java 显示 17.x
- `JAVA_HOME` 指向 JDK 17 目录

如果 Maven 版本还是 3.6.1，说明用户没重启 VS Code 或者 PATH 没生效，**停下来报告**，让用户：
1. 关闭所有 VS Code 窗口和终端
2. 重新开 VS Code
3. 重启后再继续

### 任务 2：解决中文用户名路径风险

用户 Windows 账号叫"卢凯迪"，默认 Maven 本地仓库在 `C:\Users\卢凯迪\.m2\repository`。中文路径偶尔会让某些 Maven 插件抽风。

**预防措施**：把 Maven 本地仓库改到纯英文路径。

执行：

```powershell
# 1. 创建新的仓库目录
New-Item -ItemType Directory -Path "D:\maven-repo" -Force | Out-Null

# 2. 编辑 settings.xml
# Maven 用户配置文件路径
$settingsPath = "$env:USERPROFILE\.m2\settings.xml"
$m2Dir = "$env:USERPROFILE\.m2"
if (-not (Test-Path $m2Dir)) {
    New-Item -ItemType Directory -Path $m2Dir -Force | Out-Null
}

# 检查 settings.xml 是否已存在
Test-Path $settingsPath
```

**如果 settings.xml 不存在**，创建以下内容（写入 `$settingsPath`）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">

  <!-- 本地仓库路径：避开中文用户名路径，避免 Maven 插件抽风 -->
  <localRepository>D:/maven-repo</localRepository>

  <mirrors>
    <!-- 阿里云镜像，国内下载依赖快 10 倍 -->
    <mirror>
      <id>aliyun-public</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Public Repository</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>

</settings>
```

**如果 settings.xml 已存在**，停下来告诉用户："已存在 settings.xml，请确认是否覆盖。建议查看现有内容后决定。" 不要擅自覆盖。

验证：
```powershell
mvn help:effective-settings | Select-String -Pattern "localRepository|aliyun"
```

应该能看到本地仓库指向 `D:/maven-repo`，并且 aliyun 镜像生效。

### 任务 3：创建后端项目骨架

**不要用 Spring Initializr 网页**，直接手写 `pom.xml`，更可控。

在 `D:\Claude_Playground\Soundprint\` 下创建 `backend/` 目录，然后创建 `pom.xml`，内容见后面"⚙️ 技术细节"。

执行依赖下载验证：

```powershell
cd D:\Claude_Playground\Soundprint\backend
mvn -version
mvn dependency:resolve
```

依赖第一次下载会比较久（5-10 分钟），耐心等。如果中途卡死或某个依赖下载失败：
- 检查网络（尤其是公司/学校的代理）
- 检查 settings.xml 镜像是否生效
- 不要乱改版本号

### 任务 4：建立分层包结构

在 `backend/src/main/java/com/soundprint/` 下创建以下子包（空目录即可，Maven 会忽略空目录，所以每个目录里放一个 `.gitkeep` 或先放一个占位类）：

```
com.soundprint
├── SoundprintApplication.java     ← Spring Boot 启动类
├── controller/                     ← REST 接口层
├── service/                        ← 业务服务层
│   └── impl/                       ← Service 实现
├── mapper/                         ← MyBatis-Plus Mapper 接口
├── entity/                         ← 数据库实体（与表一一对应）
├── dto/                            ← 数据传输对象
│   ├── request/                    ← 接口入参
│   └── response/                   ← 接口返回
├── config/                         ← 配置类
├── common/                         ← 通用类（Result、PageResult 等）
├── exception/                      ← 异常体系
└── util/                           ← 工具类
```

在 `src/main/resources/` 下创建：
```
resources/
├── application.yml                 ← 主配置
├── application-dev.yml             ← 开发环境配置（含数据库密码占位）
├── mapper/                         ← MyBatis XML（暂时为空，需要时再加）
└── static/                         ← 静态资源
```

### 任务 5：写启动类和基础配置

**`SoundprintApplication.java`** 内容：

```java
package com.soundprint;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Soundprint 主启动类
 *
 * @author Soundprint
 */
@SpringBootApplication
@MapperScan("com.soundprint.mapper")
public class SoundprintApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoundprintApplication.class, args);
        System.out.println("""

                ███████╗ ██████╗ ██╗   ██╗███╗   ██╗██████╗ ██████╗ ██████╗ ██╗███╗   ██╗████████╗
                ██╔════╝██╔═══██╗██║   ██║████╗  ██║██╔══██╗██╔══██╗██╔══██╗██║████╗  ██║╚══██╔══╝
                ███████╗██║   ██║██║   ██║██╔██╗ ██║██║  ██║██████╔╝██████╔╝██║██╔██╗ ██║   ██║
                ╚════██║██║   ██║██║   ██║██║╚██╗██║██║  ██║██╔═══╝ ██╔══██╗██║██║╚██╗██║   ██║
                ███████║╚██████╔╝╚██████╔╝██║ ╚████║██████╔╝██║     ██║  ██║██║██║ ╚████║   ██║
                ╚══════╝ ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝╚═════╝ ╚═╝     ╚═╝  ╚═╝╚═╝╚═╝  ╚═══╝   ╚═╝

                后端服务启动成功！
                  - API 文档:  http://localhost:8080/doc.html
                  - 健康检查:  http://localhost:8080/api/health
                """);
    }
}
```

**`application.yml`** 内容（主配置）：

```yaml
server:
  port: 8080
  servlet:
    context-path: /              # 根路径，前端访问 http://localhost:8080/api/...

spring:
  application:
    name: soundprint
  profiles:
    active: dev                  # 默认激活 dev 环境
  servlet:
    multipart:
      max-file-size: 200MB       # 单文件上传上限（FLAC 文件可能 50MB+）
      max-request-size: 500MB    # 单请求上限
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai

# MyBatis-Plus 全局配置
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true     # 下划线自动转驼峰
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # SQL 日志输出
  global-config:
    db-config:
      id-type: AUTO              # 主键自增
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# 日志
logging:
  level:
    com.soundprint: DEBUG
    org.springframework.web: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

# Knife4j API 文档
knife4j:
  enable: true
  setting:
    language: zh_cn
    enable-version: true
  documents:
    - group: 默认
      name: Soundprint API
      locations: classpath:markdown/*
```

**`application-dev.yml`** 内容（开发环境，包含数据库连接）：

⚠️ 这里需要用户提供 MySQL root 密码。**不要硬编码密码**，让用户：
1. 在终端执行 `Read-Host -AsSecureString` 读取密码
2. 把读到的密码临时写入 `application-dev.yml`
3. 提醒用户：**这个文件已经在 .gitignore 里被排除，不会被提交**（任务 6 会处理）

`application-dev.yml` 模板：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3307/soundprint?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 你的密码占位  # ← 用户手动填，文件不进 git
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

向用户输出操作指引：

```
我已经创建了 application-dev.yml 模板。请你打开这个文件：
D:\Claude_Playground\Soundprint\backend\src\main\resources\application-dev.yml

把 "你的密码占位" 这行替换成你的 MySQL root 密码（直接明文写入即可），
然后保存。

⚠️ 这个文件已加入 .gitignore，不会被提交到 GitHub。
完成后告诉我"密码已填好"，我继续后续步骤。
```

等用户确认后继续。

### 任务 6：更新 .gitignore

确保 `application-dev.yml` 不会被提交。读 `D:\Claude_Playground\Soundprint\.gitignore`，确认包含或追加：

```gitignore
# ========== Spring Boot 敏感配置 ==========
application-dev.yml
application-local.yml
application-prod.yml
application-*-local.yml
```

如果上述规则已存在（Phase 0 写过 `application-local.yml`），追加 `application-dev.yml` 即可，**不要重复添加已有的**。

### 任务 7：写基础设施类

#### `common/Result.java`（统一响应封装）

```java
package com.soundprint.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应封装
 *
 * @param <T> 返回数据类型
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }
}
```

#### `common/PageResult.java`（分页响应封装）

```java
package com.soundprint.common;

import lombok.Data;

import java.util.List;

/**
 * 分页响应封装
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> {
    private List<T> records;       // 当前页数据
    private Long total;            // 总条数
    private Long current;          // 当前页码
    private Long size;             // 每页大小
    private Long pages;            // 总页数

    public static <T> PageResult<T> of(List<T> records, Long total, Long current, Long size) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        result.setPages((total + size - 1) / size);
        return result;
    }
}
```

#### `exception/BusinessException.java`（业务异常）

```java
package com.soundprint.exception;

import lombok.Getter;

/**
 * 业务异常基类
 * 由业务代码主动抛出，会被 GlobalExceptionHandler 捕获并返回友好提示
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
```

#### `exception/GlobalExceptionHandler.java`（全局异常处理）

```java
package com.soundprint.exception;

import com.soundprint.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 捕获所有未被业务代码处理的异常，统一返回 Result 格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：业务代码主动抛出，用户输入错误等
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 兜底异常：所有未预期的异常，返回 500
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "服务器内部错误：" + e.getMessage());
    }
}
```

#### `config/MybatisPlusConfig.java`（MP 配置）

```java
package com.soundprint.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 全局配置
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 注册分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

#### `config/CorsConfig.java`（跨域配置）

```java
package com.soundprint.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置
 * 允许前端 Vite (默认 5173 端口) 访问后端 API
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:5174", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

#### `config/Knife4jConfig.java`（API 文档）

```java
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
```

### 任务 8：用 MyBatis-Plus Generator 生成 Entity + Mapper

**重要**：MP Generator 是一个独立工具，不是后端的运行时依赖。我们把它作为一个**单独的 Maven 项目模块或独立可执行 Java 类**来用，只跑一次生成代码。

创建生成器类 `D:\Claude_Playground\Soundprint\backend\src\main\java\com\soundprint\util\CodeGenerator.java`：

```java
package com.soundprint.util;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;
import java.util.Scanner;

/**
 * MyBatis-Plus 代码生成器
 *
 * ⚠️ 这是开发期工具类，运行后即可删除，不参与生产构建。
 * 运行方式：在 IDE 中右键 main() → Run
 */
public class CodeGenerator {

    public static void main(String[] args) {
        // 从控制台读取数据库密码，避免硬编码
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入 MySQL root 密码: ");
        String password = scanner.nextLine();

        String projectPath = System.getProperty("user.dir");
        String javaDir = projectPath + "/src/main/java";
        String mapperXmlDir = projectPath + "/src/main/resources/mapper";

        FastAutoGenerator.create(
                "jdbc:mysql://localhost:3307/soundprint?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true",
                "root",
                password
        )
        .globalConfig(builder -> builder
                .author("Soundprint")
                .outputDir(javaDir)
                .disableOpenDir()  // 生成后不打开目录
                .commentDate("yyyy-MM-dd")
        )
        .packageConfig(builder -> builder
                .parent("com.soundprint")
                .entity("entity")
                .mapper("mapper")
                .service("service")
                .serviceImpl("service.impl")
                .controller("controller")
                .pathInfo(Collections.singletonMap(OutputFile.xml, mapperXmlDir))
        )
        .strategyConfig(builder -> builder
                .addInclude(
                        "user", "artist", "album", "track",
                        "playlist", "playlist_track",
                        "tag", "track_tag",
                        "play_history", "conversion_task", "user_favorite"
                )
                .entityBuilder()
                    .enableLombok()
                    .enableTableFieldAnnotation()
                    .logicDeleteColumnName("is_deleted")
                    .addTableFills(new com.baomidou.mybatisplus.generator.fill.Column("created_at",
                            com.baomidou.mybatisplus.annotation.FieldFill.INSERT))
                    .addTableFills(new com.baomidou.mybatisplus.generator.fill.Column("updated_at",
                            com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE))
                .mapperBuilder()
                    .enableMapperAnnotation()
                    .enableBaseResultMap()
                    .enableBaseColumnList()
                .serviceBuilder()
                    .formatServiceFileName("%sService")
                    .formatServiceImplFileName("%sServiceImpl")
                .controllerBuilder()
                    .enableRestStyle()
        )
        .templateEngine(new FreemarkerTemplateEngine())
        .execute();

        System.out.println("代码生成完成！请查看 src/main/java/com/soundprint/ 下的新文件。");
    }
}
```

向用户输出操作指引：

```
请在 IntelliJ IDEA / VS Code 中找到这个文件：
backend/src/main/java/com/soundprint/util/CodeGenerator.java

右键 main() → Run（或者在终端执行 mvn 命令运行，但 IDE 更方便）。

运行后会要求你输入 MySQL 密码。输入后会生成 11 张表对应的：
- Entity 类 (entity/)
- Mapper 接口 (mapper/)
- Service 接口和实现 (service/, service/impl/)
- Controller (controller/)

生成完告诉我，我们检查文件 + 删除不需要的 Controller（我们只保留 TrackController，其他的 Phase 3 再写）。
```

等用户跑完生成器。

### 任务 9：清理生成的代码

生成器会为 11 张表都生成 Controller、Service、ServiceImpl。我们 Phase 2 只想保留一个 `TrackController` 跑通流程，**其他的 Controller / Service 暂时删除**，Phase 3 再按业务需要重写。

执行：

```powershell
$controllerDir = "D:\Claude_Playground\Soundprint\backend\src\main\java\com\soundprint\controller"
$serviceDir = "D:\Claude_Playground\Soundprint\backend\src\main\java\com\soundprint\service"
$serviceImplDir = "D:\Claude_Playground\Soundprint\backend\src\main\java\com\soundprint\service\impl"

# 列出生成的所有 Controller
Get-ChildItem $controllerDir -Filter "*.java" | Select-Object Name
```

向用户报告生成的 Controller 列表，然后**保留 `TrackController.java`，删除其他 10 个 Controller**：

```powershell
# 保留 TrackController.java，删除其他
Get-ChildItem $controllerDir -Filter "*.java" | Where-Object { $_.Name -ne "TrackController.java" } | Remove-Item
```

**Service 和 ServiceImpl 都保留**（11 张表的都留着，Phase 3 会用到）。

**Entity 和 Mapper 全部保留**（11 张表的都留着）。

### 任务 10：写第一个测试 API（HealthController + 改造 TrackController）

#### `controller/HealthController.java`

```java
package com.soundprint.controller;

import com.soundprint.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查
 */
@Tag(name = "系统-健康检查", description = "服务存活探测")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Operation(summary = "健康检查")
    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "soundprint-backend");
        data.put("time", LocalDateTime.now());
        return Result.success(data);
    }
}
```

#### 改造 `TrackController.java`（覆盖生成器生成的）

```java
package com.soundprint.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soundprint.common.PageResult;
import com.soundprint.common.Result;
import com.soundprint.entity.Track;
import com.soundprint.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 曲目接口
 */
@Tag(name = "曲目", description = "音乐曲目相关接口")
@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;

    @Operation(summary = "分页查询曲目")
    @GetMapping
    public Result<PageResult<Track>> listTracks(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size
    ) {
        Page<Track> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Track> wrapper = new LambdaQueryWrapper<Track>()
                .orderByDesc(Track::getCreatedAt);

        Page<Track> result = trackService.page(pageParam, wrapper);

        return Result.success(PageResult.of(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        ));
    }
}
```

### 任务 11：启动后端并验证

```powershell
cd D:\Claude_Playground\Soundprint\backend
mvn spring-boot:run
```

**预期输出**：
- 看到 Soundprint ASCII Logo
- `Tomcat started on port 8080`
- 没有 `ERROR` 字样

**如果启动失败**，常见错误和排查：
- `Communications link failure` → MySQL 没启动或端口错（应该是 3307）
- `Access denied for user` → 密码错
- `Unknown database 'soundprint'` → 数据库没建
- `Table 'soundprint.xxx' doesn't exist` → 种子数据没导入

**启动成功后，浏览器访问验证**：

1. **健康检查**：http://localhost:8080/api/health
   预期返回：
   ```json
   {
     "code": 200,
     "message": "success",
     "data": {
       "status": "UP",
       "service": "soundprint-backend",
       "time": "2026-05-24 23:30:00"
     },
     "timestamp": 1716583800000
   }
   ```

2. **曲目分页查询**：http://localhost:8080/api/tracks?page=1&size=5
   预期返回 5 条曲目数据（种子数据中的，比如 Bohemian Rhapsody）

3. **Knife4j API 文档**：http://localhost:8080/doc.html
   预期看到一个漂亮的文档界面，能在线测试上面两个接口

**向用户输出 3 个 URL 让他/她在浏览器打开**，把截图发回来。

### 任务 12：commit

确认所有文件就位后：

```powershell
cd D:\Claude_Playground\Soundprint
git status
git add backend/ .gitignore
git status  # 再看一次，确认 application-dev.yml 没在列表里

# 用 BOM-free 临时文件做中文 commit
@"
feat: 完成后端骨架与第一个 API（Phase 2）

- Spring Boot 3.2 + MyBatis-Plus 3.5 工程初始化
- 连接 MySQL 8.0 @ localhost:3307
- 分层包结构（controller/service/mapper/entity/dto/config/common/exception）
- 基础设施类：Result、PageResult、BusinessException、GlobalExceptionHandler
- 跨域配置（CorsConfig）+ Knife4j API 文档
- MyBatis-Plus Generator 生成 11 张表的 Entity/Mapper/Service
- 实现 HealthController + TrackController（分页查询打通）
- application-dev.yml 已加入 .gitignore，密码不进仓库
"@ | Out-File "D:\Claude_Playground\_msg.txt" -Encoding utf8NoBOM

git commit -F "D:\Claude_Playground\_msg.txt"
Remove-Item "D:\Claude_Playground\_msg.txt"
```

**push 与否听用户的**，按 CLAUDE.md 不擅自推。

---

## ⚙️ 技术细节 / 文件内容

### 完整 `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>

    <groupId>com.soundprint</groupId>
    <artifactId>soundprint-backend</artifactId>
    <version>1.0.0</version>
    <name>soundprint-backend</name>
    <description>Soundprint 后端服务</description>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <mybatis-plus.version>3.5.7</mybatis-plus.version>
        <knife4j.version>4.5.0</knife4j.version>
        <jaudiotagger.version>3.0.1</jaudiotagger.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MyBatis-Plus（Spring Boot 3 专用 starter） -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- MyBatis-Plus Generator（代码生成器） -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-generator</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- Freemarker（生成器模板引擎） -->
        <dependency>
            <groupId>org.freemarker</groupId>
            <artifactId>freemarker</artifactId>
        </dependency>

        <!-- MySQL Driver -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Knife4j（Swagger 增强 UI，Spring Boot 3 版本） -->
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
            <version>${knife4j.version}</version>
        </dependency>

        <!-- jaudiotagger（音频元数据读取，Phase 3 用） -->
        <dependency>
            <groupId>net.jthink</groupId>
            <artifactId>jaudiotagger</artifactId>
            <version>${jaudiotagger.version}</version>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>soundprint-backend</finalName>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

---

## 📚 边写边讲要求

Claude Code 在本阶段必须主动给用户讲清楚以下技术点（**用户是 Spring 框架新手，每个点都要展开**）：

### 1. Spring Boot 是什么、为什么用 `@SpringBootApplication`（必讲）
- 自动配置、起步依赖、约定大于配置
- 答辩话术

### 2. 分层架构（Controller → Service → Mapper → DB）（必讲）
- 每层职责
- 为什么不能在 Controller 直接写 SQL

### 3. MyBatis-Plus vs 原生 MyBatis vs JPA（讲）
- 课程为什么选 MP
- 增删改查不写 SQL 的原理

### 4. `application.yml` 关键配置讲解（必讲）
- profile（dev / prod 分环境）
- HikariCP 连接池参数含义
- multipart 大小限制为什么调到 200MB
- `map-underscore-to-camel-case` 干了什么

### 5. 统一响应封装 `Result<T>` 为什么要有（讲）
- 前后端契约
- 全栈一致性

### 6. 全局异常处理 `@RestControllerAdvice`（讲）
- AOP 思想
- 用户体验

### 7. 跨域 CORS（必讲）
- 同源策略
- 为什么前端 5173 调后端 8080 需要配置 CORS

### 8. Knife4j 是什么、解决了什么（讲）
- Swagger / OpenAPI
- 前端没写完也能测后端

### 9. Lombok 的 `@Data`、`@RequiredArgsConstructor`（讲）
- 注解处理器原理
- 编译期生成代码

### 10. 一定要在写完后给出"如果启动失败怎么排查"指南
- 数据库连不上的 5 种可能
- 端口被占用怎么改
- 看日志的方法

---

## ✅ 完成检查清单

- [ ] `mvn -version` 显示 3.9.16，Java 17
- [ ] Maven 本地仓库已改到 `D:\maven-repo`
- [ ] `backend/` 目录创建完毕，`pom.xml` 已写
- [ ] 依赖下载成功（无 Maven 报错）
- [ ] 分层包结构完整
- [ ] 基础设施类全部就位（Result、PageResult、BusinessException、GlobalExceptionHandler、各 Config）
- [ ] `application.yml` + `application-dev.yml` 配置完成
- [ ] `application-dev.yml` 在 .gitignore 中（`git status` 看不到它）
- [ ] MyBatis-Plus Generator 跑通，11 张表的 Entity/Mapper/Service 已生成
- [ ] 其余 10 个 Controller 已删除，只保留 TrackController
- [ ] HealthController 写好
- [ ] `mvn spring-boot:run` 启动成功，看到 Logo
- [ ] http://localhost:8080/api/health 返回 200
- [ ] http://localhost:8080/api/tracks?page=1&size=5 返回种子数据
- [ ] http://localhost:8080/doc.html 能访问，能在线测试接口
- [ ] commit 已完成（push 听用户的）

---

## 📩 反馈给架构师的内容

完成本阶段后，请反馈：

1. **mvn -version 输出**
2. **`mvn spring-boot:run` 启动日志**（前 30 行 + 后 10 行）
3. **3 个 URL 的截图**：
   - `/api/health` 返回值
   - `/api/tracks?page=1&size=5` 返回值
   - `/doc.html` 文档界面
4. **`backend/` 目录结构**（用 `tree` 或 `Get-ChildItem -Recurse` 输出，去掉 target 和 node_modules）
5. **Claude Code 讲解你听不懂的地方**
6. **是否准备好进入 Phase 3（后端业务接口）**

---

## ⚠️ 注意事项

- **不要修改种子数据，不要 DROP 表**
- **不要把 application-dev.yml 提交进 git**
- **不要在 pom.xml 里乱加依赖**，需要新依赖时先问用户
- **不要擅自降 Spring Boot 版本或 Java 版本**
- **运行 CodeGenerator 时，密码通过 Scanner 读取**，不要传命令行参数
- 如果 mvn 下载某个依赖很慢/失败，**停下来报告**，不要自己尝试改源

---

**End of Phase 2 Document.**
