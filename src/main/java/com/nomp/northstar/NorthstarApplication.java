package com.nomp.northstar;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@MapperScan("com.nomp.northstar.modules.**.mapper")
@ConfigurationPropertiesScan
public class NorthstarApplication {

  public static void main(String[] args) {
    SpringApplication.run(NorthstarApplication.class, args);
  }
}
