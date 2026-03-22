package com.example.hotelback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HotelbackApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelbackApplication.class, args);
	}

}
