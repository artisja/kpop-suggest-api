package com.kpopsuggest.kpopsuggest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class KpopsuggestApplication {

	public static void main(String[] args) {
		SpringApplication.run(KpopsuggestApplication.class, args);
	}

	@GetMapping("/RedVelvet")
	public String redVelvetTest(@RequestParam(value = "song", defaultValue = "Happiness") String song) {
		return String.format("Hello %s!", song);
	}

}
