package me.superning.provider.service.impl;

import me.superning.provider.server.RpcService;
import me.superning.provider.service.HelloService;

/**
 * 实现服务接口
 * @author superning
 * @date 2020年1月13日
 */
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "Hello! " + name;
    }
}
