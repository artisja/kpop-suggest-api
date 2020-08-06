package java.com.kpopsuggest;

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

import java.Model.Song;
import java.util.ArrayList;
import java.util.List;

@RestController
public class KpopsuggestApplication {
	public AmazonDynamoDB client= AmazonDynamoDBClientBuilder.standard()
			.withRegion(Regions.US_EAST_1)
			.build();
	public DynamoDB dynamoDB = new DynamoDB(client);

	public static void main(String[] args) {
		SpringApplication.run(KpopsuggestApplication.class, args);
	}

	@GetMapping("/RedVelvet")
	public String redVelvetTest(@RequestParam(value = "song", defaultValue = "Happiness") String song) {
		return String.format("Hello %s!", song);
	}

	//Gets suggestions for User
	@RequestMapping(value = "/Suggestions/add/{userId}",method = RequestMethod.GET, headers = "Accept=application/json")
	public PutItemOutcome addSuggestions(@PathVariable("userId") String userId, Song song) {

		Table suggestionsTable = dynamoDB.getTable("suggestions_table");
		GetItemSpec spec = new GetItemSpec().withPrimaryKey("userId", "zion");
		System.out.println(suggestionsTable.getItem(spec));
		List<Song> suggestSongItems = new ArrayList<Song>();
		suggestSongItems.add(song);
		PutItemOutcome outcome = suggestionsTable.putItem(suggestSongItems);
		return outcome;
	}


	public boolean isInputValid(String input){
		return input.contains("select") || input.contains("*");
	}


}
