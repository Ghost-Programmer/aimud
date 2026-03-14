package com.aimud.aimud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AimudApplication {

	public static void main(String[] args) {
		SpringApplication.run(AimudApplication.class, args);
	}

}
