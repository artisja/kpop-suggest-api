package com.kpopsuggest;

import Model.Song;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class SongDBController {

    public AmazonDynamoDB client= AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .build();
    public DynamoDB dynamoDB = new DynamoDB(client);

    @GetMapping("/RedVelvet")
    public String redVelvetTest(@RequestParam(value = "song", defaultValue = "Happiness") String song) {
        return String.format("Hello %s!", song);
    }

    //Gets suggestions for User
    @PostMapping(value = "/Suggestions/add/{userId}", headers = "Accept=application/json")
    public PutItemOutcome addSuggestions(@PathVariable("userId") String userId,@RequestBody Song song) {

        Table suggestionsTable = dynamoDB.getTable("suggestions_table");
        GetItemSpec spec = new GetItemSpec().withPrimaryKey("userId", "zion");
        System.out.println(suggestionsTable.getItem(spec));
        List<Song> suggestSongItems = new ArrayList<Song>();
        suggestSongItems.add(song);
        Item suggestItem = new Item()
                .withPrimaryKey("userId", userId)
                .withList("suggestionList", suggestSongItems);
        PutItemOutcome outcome = suggestionsTable.putItem(suggestItem);
        return outcome;
    }


    public boolean isInputValid(String input){
        return input.contains("select") || input.contains("*");
    }

}
