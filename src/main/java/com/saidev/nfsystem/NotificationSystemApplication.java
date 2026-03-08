package com.saidev.nfsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableKafka
@EnableScheduling
@SpringBootApplication
public class NotificationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationSystemApplication.class, args);
	}

}
