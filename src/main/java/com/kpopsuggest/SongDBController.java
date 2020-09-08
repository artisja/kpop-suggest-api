package com.kpopsuggest;

import Model.Song;
import Model.SongIDWrapper;
import Utils.Constants;
import Utils.SongDBUtil;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import io.netty.util.Constant;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                                .withPrimaryKey(Constants.SONG_ID.attribute, song.getSongId())
                                .withString(Constants.NAME.attribute,song.getArtistName())
                                .withInt(Constants.LENGTH.attribute,song.getLength())
                                .withInt(Constants.LIKES.attribute, song.getLikes())
                                .withString(Constants.LINK.attribute,song.getLink())
                                .withString(Constants.NAME.attribute, song.getName())
                );
        BatchWriteItemOutcome batchWriteItemOutcome =  dynamoDB.batchWriteItem(tableWriteItems);
//        System.out.println(batchWriteItemOutcome.getBatchWriteItemResult());
        return "Uploaded";
    }

    //Retrieves song using song-ids
    @GetMapping(path = "Songs/retrieve",consumes = "application/json",produces = "application/json")
    public ArrayList<Song> retrieveSong(@RequestBody SongIDWrapper songIDList){
        Table songTable = dynamoDB.getTable("song_table");
        ArrayList<Item> retrievedItems = new ArrayList<Item>();
        ArrayList<Song> retrievedSongs = new ArrayList<Song>();
        songIDList.getSongID().stream().forEach(integer -> retrievedItems.add(songTable.getItem("songId",integer)));
        SongDBUtil songDBUtil = new SongDBUtil();
        for (Item songItem: retrievedItems) {
            retrievedSongs.add(songDBUtil.transferItem(songItem.attributes(),new Song()));
        }
        return retrievedSongs;
    }


    public boolean isInputValid(String input){
        return input.contains("select") || input.contains("*");
    }

}
