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
                "jdbc:mysql://localhost:3307/soundprint?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true",
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
