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
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

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

    /**
     * Adds song
     * @param userId
     * @param song
     * @return
     */
    @PostMapping(path = "/Songs/add/{userId}", consumes = "application/json", produces = "application/json")
    public String addSong(@PathVariable("userId") String userId,@RequestBody Song song) {
        TableWriteItems tableWriteItems;
        BatchWriteItemOutcome batchWriteItemOutcome;
        try {
            SecureRandom songIDGenerator = SecureRandom.getInstance("SHA1PRNG");
            tableWriteItems = new TableWriteItems("song_table")
                    .withItemsToPut(
                            new Item()
                                    .withPrimaryKey(Constants.SONG_ID.attribute, new Integer(songIDGenerator.nextInt(Integer.MAX_VALUE)))
                                    .withString(Constants.ARTIST_NAME.attribute,song.getArtistName())
                                    .withInt(Constants.LENGTH.attribute,song.getTimeLength())
                                    .withInt(Constants.LIKES.attribute, song.getLikes())
                                    .withString(Constants.LINK.attribute,song.getLink())
                                    .withString(Constants.NAME.attribute, song.getTitle())
                    );
            batchWriteItemOutcome =  dynamoDB.batchWriteItem(tableWriteItems);
            return batchWriteItemOutcome.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "500";
    }

    @PutMapping(path = "/Songs/edit/{songID}",consumes = "application/json",produces = "application/json")
    public String editSong(@RequestBody Song song){
        Table songTable = dynamoDB.getTable("song_table");
        String result = "";
        UpdateItemSpec updateSongSpec = new UpdateItemSpec().withPrimaryKey("songId",song.getSongId())
                .withUpdateExpression("set artistName = :a,timeLength = :l,likes = :k,link = :n, title = :t")
                .withValueMap(new ValueMap().withString(":a",song.getArtistName())
                                .withInt(":l",song.getTimeLength())
                                .withInt(":k", song.getLikes())
                                .withString(":n",song.getLink())
                                .withString(":t", song.getTitle()))
                .withReturnValues(ReturnValue.UPDATED_NEW);;
        try{
            UpdateItemOutcome updateSongOutcome = songTable.updateItem(updateSongSpec);
            result = updateSongOutcome.getItem().toJSONPretty();
        }catch(Exception exception){
            System.err.println(exception.toString());
        }
        return result;
    }

    /**
     * Retrieves song
     * @param songIDList
     * @return
     */
    @GetMapping(path = "/Songs/retrieve",consumes = "application/json",produces = "application/json")
    public ArrayList<Song> retrieveSong(@RequestBody SongIDWrapper songIDList){
        Table songTable = dynamoDB.getTable("song_table");
        ArrayList<Item> retrievedItems = new ArrayList<Item>();
        ArrayList<Song> retrievedSongs = new ArrayList<Song>();
        songIDList.getSongID().stream().forEach(integer -> retrievedItems.add(songTable.getItem("songId",integer)));
        SongDBUtil songDBUtil = new SongDBUtil();
        for (Item songItem: retrievedItems) {
            retrievedSongs.add(songDBUtil.transferItem(songItem.attributes().iterator(),new Song()));
        }
        return retrievedSongs;
    }


    public boolean isInputValid(String input){
        return input.contains("select") || input.contains("*");
    }

}
