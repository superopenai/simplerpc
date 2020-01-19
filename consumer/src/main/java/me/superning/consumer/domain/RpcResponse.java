package me.superning.consumer.domain;

import lombok.Data;

/**
 * @author superning
 * @date 2020年1月15日
 */
@Data
public class RpcResponse {
    //响应ID
    private String reponseId;
    //异常对象
    private Throwable error;
    //响应结果
    private Object result;
}
