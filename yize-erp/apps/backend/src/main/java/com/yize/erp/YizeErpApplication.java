package com.yize.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@MapperScan("com.yize.erp.**.mapper")
@SpringBootApplication
public class YizeErpApplication {
    public static void main(String[] args) {
        SpringApplication.run(YizeErpApplication.class, args);
    }
}
