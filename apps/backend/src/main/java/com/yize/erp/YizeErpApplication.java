package com.yize.erp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.yize.erp.**.mapper")
@SpringBootApplication
public class YizeErpApplication {
    public static void main(String[] args) {
        SpringApplication.run(YizeErpApplication.class, args);
    }
}
