package org.example.studiopick;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@MapperScan("org.example.studiopick.infrastructure.mybatis")
public class StudioPickApplication {

  public static void main(String[] args) {
    SpringApplication.run(StudioPickApplication.class, args);
  }

}
