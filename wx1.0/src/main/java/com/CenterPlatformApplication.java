package com;

import com.lx.entity.WxMessage;
import com.lx.service.QueryService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.util.Properties;

//mvn install 打包
//@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)//不加载数据源
@SpringBootApplication
@MapperScan({"com.lx"})//扫包
//@PropertySource(value={"file:C:/application.properties"})//读取配置文件
public class CenterPlatformApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CenterPlatformApplication.class, args);
    }
//    @Bean
//    public ConfigurableServletWebServerFactory webServerFactory() {
//        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
//        factory.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> connector.setProperty("relaxedQueryChars", "|{}[]\\"));
//        return factory;
//    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(CenterPlatformApplication.class);
    }

//    @Test
    public void contextLoads() throws Exception {
        ApplicationContext a = SpringApplication.run(CenterPlatformApplication.class);
        QueryService queryService = a.getBean(QueryService.class);
        queryService.wx_in(new WxMessage("【这个#聚划算团购#宝贝不错:夏季冰防晒男袖套防紫外线手套女薄冰丝袖子开车骑行护臂手臂套袖(分享自@手机淘宝android客户端)】https://m.tb.cn/h.e5BBCA6 点击链接，再选择浏览器咑閞；或復·制这段描述￥ycGHYUBr4Y7￥后到<span class=\"emoji emoji1f449\"></span>淘♂寳♀<span class=\"emoji emoji1f448\"></span>"
                ,"","lxzz_000002","lx",""));
    }

}
