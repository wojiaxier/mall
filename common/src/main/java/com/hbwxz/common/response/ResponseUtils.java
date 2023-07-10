package com.hbwxz.common.response;

public class ResponseUtils {
    //成功
    public static CommonResponse okResponse (Object content) {
        return CommonResponse.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .message(ResponseCode.SUCCESS.getMessage())
                .content(content)
                .build();
    }
    //失败
    public static CommonResponse failResponse (Integer code , String errorMessage , Object content) {
        return CommonResponse.builder()
                .code(code)
                .message(errorMessage)
                .content(content)
                .build();
    }
}
