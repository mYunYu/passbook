package com.jju.passbook.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  Controller 统一的响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    //错误码：正确返回 0
    private Integer errorCode = 0;

    //错误信息，正确返回空字符串
    private String errorMsg = "";

    //返回值对象
    private Object data;

    public Response(Object data) {
        this.data = data;
    }

    /**
     *  正确响应
     * @return
     */
    public static Response success(){
        return new Response();
    }

    /**
     *  错误响应
     * @param errorMsg
     * @return
     */
    public static Response failure(String errorMsg){
        return new Response(-1, errorMsg, null);
    }

}
