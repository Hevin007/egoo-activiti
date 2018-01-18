package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.example","org.activiti"})
public class SpringBootWithActivitiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWithActivitiApplication.class, args);
	}
}
