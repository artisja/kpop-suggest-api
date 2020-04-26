package com.kpopsuggest.kpopsuggest;

import Model.Artist;
import Model.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

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

	@RequestMapping(value = "/Suggestions/{userId}",method = RequestMethod.GET, headers = "Accept=application/json")
	public ArrayList<Artist> getUserSuggest(@PathVariable("userId") String userId) {
		User user = new User();
		user.setUsername(userId);
		ArrayList artistList = new ArrayList();
		if (isInputValid(user.getUsername())) {
			return new ArrayList<>();
		}else {
			Artist newArtist = new Artist();
			newArtist.setName("Red Velvet");
			newArtist.setLikes(10);
			artistList.add(newArtist);
		}

		return artistList;
	}


	public boolean isInputValid(String input){
		return input.contains("select") || input.contains("*");
	}


}
