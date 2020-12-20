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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistRequest;
import net.minidev.json.JSONObject;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import com.wrapper.spotify.SpotifyApi;

import javax.annotation.PostConstruct;

@RestController
public class ArtistDBController {

    @Value("${client.id}")
    private String clientId;

    @Value("${client.secret}")
    private String clientSecret;

    private SpotifyApi spotifyApi;
    @PostConstruct
    public void init(){
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
                .build();
        try {
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
        } catch (IOException | SpotifyWebApiException | ParseException  e) {
            e.printStackTrace();
        }
    }

    private AmazonDynamoDB client= AmazonDynamoDBClientBuilder.standard()
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
        BatchWriteItemOutcome batchWriteItemOutcome;
        try {
            batchWriteItemOutcome =  dynamoDB.batchWriteItem(convertSong(song));
            return batchWriteItemOutcome.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "500";
    }

    public TableWriteItems convertSong(Song song) throws NoSuchAlgorithmException{
        SecureRandom songIDGenerator = SecureRandom.getInstance("SHA1PRNG");
        return new TableWriteItems("song_table")
                .withItemsToPut(
                        new Item()
                                .withPrimaryKey(Constants.SONG_ID.attribute, new Integer(songIDGenerator.nextInt(Integer.MAX_VALUE)))
                                .withString(Constants.ARTIST_NAME.attribute,song.getArtistName())
                                .withInt(Constants.LENGTH.attribute,song.getTimeLength())
                                .withInt(Constants.LIKES.attribute, song.getLikes())
                                .withString(Constants.LINK.attribute,song.getLink())
                                .withString(Constants.NAME.attribute, song.getTitle())
                );
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
    @ResponseStatus(HttpStatus.FOUND)
    public ResponseEntity retrieveSongs(@RequestBody SongIDWrapper songIDList){
        ObjectMapper songJsonMapper = new ObjectMapper();
        Table songTable = dynamoDB.getTable("song_table");
        ArrayList<Item> retrievedItems = new ArrayList<Item>();
        ArrayList<Song> retrievedSongs = new ArrayList<Song>();
        songIDList.getSongIDs().stream().forEach(integer -> retrievedItems.add(songTable.getItem("songId",integer)));
        SongDBUtil songDBUtil = new SongDBUtil();
        //may want to add a save for if songID not found
        for (Item songItem: retrievedItems) {
            if(songItem!=null){
                retrievedSongs.add(songDBUtil.transferItem(songItem.attributes().iterator(),new Song()));
            }
        }
        String songsJson = "";
        try {
            songsJson = songJsonMapper.writeValueAsString(retrievedSongs);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if(retrievedSongs.size()!=songIDList.getSongIDs().size()){
            return new ResponseEntity<>(
                    songsJson,
                    HttpStatus.PARTIAL_CONTENT);
        }

        return new ResponseEntity<>(
                songsJson,
                HttpStatus.FOUND);
    }

    /**
     *
     * @param artistId
     * @return
     */
    @PutMapping(path = "/Artist/add",consumes = "application/json",produces = "application/json")
    public String addArtist(@RequestBody String artistId){
        if(isInputInvalid(artistId)){
            try {
                throw new Exception();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        GetArtistRequest getArtistRequest = spotifyApi.getArtist(artistId).build();
        Artist artist = null;
        try {
            artist = getArtistRequest.execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }
        JSONObject json = new JSONObject();
        json.put("name",artist.getName());
        json.put("genre",artist.getGenres());
        json.put("followers",artist.getFollowers().toString());
        return json.toJSONString();
    }


    public boolean isInputInvalid(String input){
        return (input.contains("select") || input.contains("*"));
    }

}