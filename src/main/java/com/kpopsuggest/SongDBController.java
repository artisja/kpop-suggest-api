package com.kpopsuggest;

import Model.Song;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.*;
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
    DynamoDBMapper dbMapper = new DynamoDBMapper(client);

    @GetMapping("/RedVelvet")
    public String redVelvetTest(@RequestParam(value = "song", defaultValue = "Happiness") String song) {
        return String.format("Hello %s!", song);
    }

    //Posts suggestions for User
    @PostMapping(path = "/Songs/add/{userId}", consumes = "application/json", produces = "application/json")
    public String addSong(@PathVariable("userId") String userId,@RequestBody Song song) {
        TableWriteItems tableWriteItems = new TableWriteItems("song_table")
                .withItemsToPut(
                        new Item()
                                .withPrimaryKey("songId", song.getSongId())
                                .withString("artistName",song.getArtistName())
                                .withInt("length",song.getLength())
                                .withInt("likes", song.getLikes())
                                .withString("link",song.getLink())
                                .withString("name", song.getName())
                );
        BatchWriteItemOutcome batchWriteItemOutcome =  dynamoDB.batchWriteItem(tableWriteItems);
        System.out.println(batchWriteItemOutcome.getBatchWriteItemResult());
        return "Uploaded";
    }

    @GetMapping(path = "Songs/retrieve",consumes = "application/json",produces = "application/json")
    public ArrayList<Song> retrieveSong(@RequestBody List<Integer> songsList){
        Table songTable = dynamoDB.getTable("song_table");
        ArrayList<Song> retrievedSongs = new ArrayList<Song>();
        songsList.stream().forEach(integer -> System.out.println(songTable.getItem("SongId",integer)));
        return retrievedSongs;
    }


    public boolean isInputValid(String input){
        return input.contains("select") || input.contains("*");
    }

}
