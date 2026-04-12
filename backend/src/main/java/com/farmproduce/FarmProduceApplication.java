package com.farmproduce;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FarmProduceApplication {

    public static void main(String[] args) {
        System.out.println(StringUtils.abbreviate("farm-produce-api", 20));
        SpringApplication.run(FarmProduceApplication.class, args);
    }
}
