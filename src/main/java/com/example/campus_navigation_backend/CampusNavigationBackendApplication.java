package com.example.campus_navigation_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CampusNavigationBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampusNavigationBackendApplication.class, args);
	}

}
