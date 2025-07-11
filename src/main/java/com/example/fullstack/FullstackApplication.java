package com.example.fullstack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class FullstackApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Budapest"));

		SpringApplication.run(FullstackApplication.class, args);
	}

}
