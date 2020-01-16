package me.superning.provider.domain;

import lombok.Data;

/**
 * @author superning
 * @date 2020年1月15日
 */
@Data
public class RpcRequest {
    //请求ID
    private String requestId;
    //调用class类名
    private String className;
    //调用方法名
    private String methodName;
    //调用参数类型集合
    private Class<?>[] parameterTypes;
    //调用参数集合
    private Object[] parameters;

}
