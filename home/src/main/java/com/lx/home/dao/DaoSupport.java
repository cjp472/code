package com.lx.home.dao;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Repository;

/**
 * Created by 游林夕 on 2019/9/19.
 */
@Repository
public class DaoSupport implements ApplicationContextAware{
    public static Dao dao;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        dao = applicationContext.getBean(Dao.class);
    }
}
