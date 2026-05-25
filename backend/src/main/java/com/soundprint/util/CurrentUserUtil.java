package com.soundprint.util;

import com.soundprint.config.CurrentUserProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 当前登录用户工具（临时方案）
 *
 * 现在固定返回配置里的 current-user-id。
 * Phase 4 接入登录后，这里改成从 SecurityContextHolder 取，
 * 所有调用方（Service）代码无需改动——这就是依赖反转/单一入口的好处。
 */
@Component
@RequiredArgsConstructor
public class CurrentUserUtil {

    private final CurrentUserProperties props;

    public Long getCurrentUserId() {
        return props.getCurrentUserId();
    }
}
