package com.lx.orderPlatform.util;

import com.lx.orderPlatform.service.pay.OnlinePay;
import com.lx.orderPlatform.service.pay.alipay.Base64;
import com.lx.util.LX;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * Created by 游林夕 on 2019/8/16.
 */
@Component
public class Tools implements ApplicationContextAware,EnvironmentAware {
    private ApplicationContext application;
    private Environment environment;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.application = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getProperty(String key){
        return environment.getProperty(key);
    }
    public <T> T getBean(String id, Class<T> tClass) {
        return application.getBean(id,tClass);
    }
}
