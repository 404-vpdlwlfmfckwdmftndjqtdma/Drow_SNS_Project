package com.canvasflow.global.exception;

import lombok.Getter;

@Getter
public class CanvasflowException extends RuntimeException {

    private final ErrorCode errorCode;

    public CanvasflowException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
