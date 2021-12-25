package com.bepigkiller.dommo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.bepigkiller.dommo.mapper")
public class DommoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DommoApplication.class, args);
    }

}
