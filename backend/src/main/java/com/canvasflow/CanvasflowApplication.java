package com.canvasflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class CanvasflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(CanvasflowApplication.class, args);
    }
}
