package com.jju.passbook.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  统一的错误信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorInfo<T> {

    //错误码
    public static final Integer ERROR = -1;

    //特定的错误码
    private Integer code;

    //错误信息
    private String message;

    //请求 URL
    private String url;

    //请求返回数据
    private T data;
}
