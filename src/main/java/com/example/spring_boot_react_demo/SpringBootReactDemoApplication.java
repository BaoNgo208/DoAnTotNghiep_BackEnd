package com.example.spring_boot_react_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SpringBootReactDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactDemoApplication.class, args);
	}

}
