package com.soundprint.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 *
 * 实体上的 @TableField(fill = FieldFill.INSERT / INSERT_UPDATE) 只是声明意图，
 * 真正写入值需要这个 handler。没有它，created_at/updated_at 在插入时会是 NULL，
 * 触发数据库 NOT NULL 约束报错。
 *
 * strictInsertFill / strictUpdateFill 只会对"实体里确实存在且标了对应 fill"的字段生效，
 * 所以对没有 updated_at 的表（如 conversion_task）是安全的无操作。
 */
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
