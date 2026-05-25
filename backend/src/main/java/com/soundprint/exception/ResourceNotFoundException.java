package com.soundprint.exception;

/**
 * 资源不存在异常（404）
 * 用于"按 id 查不到"的统一抛出。
 */
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, Object id) {
        super(404, String.format("%s 不存在 (id=%s)", resource, id));
    }
}
