package com.webbeat.webbeat;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WebbeatApplication {

	public static void main(String[] args) {SpringApplication.run(WebbeatApplication.class, args);}

}
