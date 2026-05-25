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
