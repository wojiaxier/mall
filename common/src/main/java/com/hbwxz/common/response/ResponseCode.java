package com.hbwxz.common.response;

public enum ResponseCode {

    SUCCESS(200,"Request successfully"),
    BAD_REQUEST(400,"Bad Request"),
    UNAUTHORIZED(401,"Unauthorized");

    private Integer code;

    private String message;

    private ResponseCode (Integer code , String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
