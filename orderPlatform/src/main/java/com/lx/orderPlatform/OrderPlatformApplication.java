package com.lx.orderPlatform;

import com.fh.util.PageData;
import com.lx.util.LX;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

/**
 * @author 游林夕
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@ComponentScan("com.lx")
public class OrderPlatformApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(OrderPlatformApplication.class, args);
	}

}
