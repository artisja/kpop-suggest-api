package com.kpopsuggest.kpopsuggest;

import Model.Artist;
import Model.User;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.amazonaws.regions.Regions;
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
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
				.withRegion(Regions.US_EAST_1)
				.build();

		DynamoDB dynamoDB = new DynamoDB(client);
		Table songTable = dynamoDB.getTable("song_table");
		GetItemSpec spec = new GetItemSpec().withPrimaryKey("songId", 1);
		System.out.println(songTable.getItem(spec));
		User user = new User();
		user.setUsername(userId);
		ArrayList artistList = new ArrayList();
		if (isInputValid(user.getUsername())) {
			//get user from database
			return new ArrayList<>();
		} else {
			//Random suggestion selections from song table
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
