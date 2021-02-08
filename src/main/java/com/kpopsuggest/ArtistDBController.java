package com.kpopsuggest;

import Model.Song;
import Model.SongIDWrapper;
import Model.SongNotFoundException;
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
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchArtistsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import net.minidev.json.JSONObject;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;

import com.wrapper.spotify.SpotifyApi;

import javax.annotation.PostConstruct;
import javax.naming.Name;

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
     * @param songName
     * @return
     */
    @PutMapping(path = "/Song/add/{songName}", produces = "application/json")
    public String addSong(@PathVariable("songName") String songName) {
        Paging<Track>  trackPaging = null;
        try {
            trackPaging =  searchSong(songName);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        BatchWriteItemOutcome batchWriteItemOutcome = null;
        Track topTrack = trackPaging.getItems()[0];
        try {
            batchWriteItemOutcome = dynamoDB.batchWriteItem(convertTrackToItem(topTrack));
        } catch (NoSuchAlgorithmException e) {
            JSONObject json = new JSONObject();
            json.put("Status", HttpStatus.CONFLICT);
            json.put("Result","Duplicate Song");
            return json.toJSONString();
        }
        JSONObject json = new JSONObject();
        json.put("Status", HttpStatus.CREATED);
        json.put("Result", batchWriteItemOutcome.getBatchWriteItemResult().toString());
        json.put("link","/Song/"+topTrack.getId());
        return json.toJSONString();
    }

    public TableWriteItems convertTrackToItem(Track track) throws NoSuchAlgorithmException{
        return new TableWriteItems(Constants.TABLE.attribute)
                .withItemsToPut(
                        new Item()
                                .withPrimaryKey(Constants.SONG_ID.attribute, track.getId())
                                .withString(Constants.ARTIST_NAME.attribute,track.getArtists()[0].getName())
                                .withInt(Constants.LENGTH.attribute,track.getDurationMs())
                                .withInt(Constants.LIKES.attribute, track.getPopularity())
                                .withString(Constants.LINK.attribute,track.getUri())
                                .withString(Constants.NAME.attribute, String.valueOf(track.getName()))
                );
    }

    @PutMapping(path = "/Songs/update/{songId}",consumes = "application/json",produces = "application/json")
    public String updateSong(@RequestBody Song song,@PathVariable String songId){
        Table songTable = dynamoDB.getTable(Constants.TABLE.attribute);
        String result = "";
        String updateExpression = buildUpdateExpression(song);

        UpdateItemSpec updateSongSpec = new UpdateItemSpec().withPrimaryKey("songId",songId)
                .withUpdateExpression(updateExpression)
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

    private String buildUpdateExpression(Song song) {
        StringBuilder updateExpressionBuilder = new StringBuilder();
        updateExpressionBuilder.append("set ");
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> props = mapper.convertValue(song, Map.class);
        for(String propsKeys :props.keySet()) {
            switch (propsKeys){
                case "artistName": updateExpressionBuilder.append("artistName = :a,");
                    break;
                case "length": updateExpressionBuilder.append("timeLength = :l,");
                    break;
                case "link": updateExpressionBuilder.append("link = :n,");
                    break;
                case "songName": updateExpressionBuilder.append("songName = :t,");
                    break;
                case "likes": updateExpressionBuilder.append("likes = :k,");
                    break;
            }
        }
        updateExpressionBuilder.deleteCharAt(updateExpressionBuilder.lastIndexOf(","));
       return updateExpressionBuilder.toString();
    }

    /**
     *
     * Get Song
     * @param songName
     * @return Json
     */
    @GetMapping(value = "/Song/{songName}",produces = "application/json")
    @ResponseStatus(HttpStatus.FOUND)
    public String getSong(@PathVariable("songName") String songName) {
        //check database for song if not there then will need to send suggestion
        Paging<Track> trackPaging = null;
        try {
            trackPaging = searchSong(songName);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        Track song = trackPaging.getItems()[0];
        ObjectMapper objectMapper = new ObjectMapper();
        String response = null;
        try {
           response = objectMapper.writeValueAsString(song);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return response;
    }

    private Paging<Track> searchSong(String songName) throws Exception {
        SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(songName + " genre:k-pop").build();
        Paging<Track> trackPaging = null;
        try {
            trackPaging = searchTracksRequest.execute();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (SpotifyWebApiException spotifyWebApiException) {
            spotifyWebApiException.printStackTrace();
        } catch (ParseException parseException) {
            parseException.printStackTrace();
        }

        if(trackPaging.getItems().length==0){
           throw new SongNotFoundException("Song not found");
        }

        return trackPaging;
    }

    /**
     * Retrieves song
     * @param songIDList
     * @return Json
     */
    @GetMapping(path = "/Songs/retrieve",consumes = "application/json",produces = "application/json")
    @ResponseStatus(HttpStatus.FOUND)
    public ResponseEntity retrieveSongs(@RequestBody SongIDWrapper songIDList){
        ObjectMapper songJsonMapper = new ObjectMapper();
        String songsJson = "";
        Table songTable = dynamoDB.getTable(Constants.TABLE.attribute);
        ArrayList<Item> retrievedItems = new ArrayList<Item>();
        ArrayList<Song> retrievedSongs = new ArrayList<Song>();
        try{
         songIDList.getSongIDs().stream().forEach(
                songId -> retrievedItems.add(songTable.getItem("songId",songId))
         );
        }catch (Exception exception){
            return new ResponseEntity<>(
                    exception.getCause(),
                    HttpStatus.NOT_FOUND);
        }
        SongDBUtil songDBUtil = new SongDBUtil();
        //may want to add a save for if songID not found
        for (Item songItem: retrievedItems) {
            if(songItem!=null){
                retrievedSongs.add(songDBUtil.transferItem(songItem.attributes().iterator(),new Song()));
            }
        }
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
     * @param artistName
     * @return
     */
    @GetMapping(path = "/search/artist/{artist}",consumes = "application/json",produces = "application/json")
    public String searchArtist(@PathVariable("artist") String artistName){
        if(isInputInvalid(artistName)){
            try {
                throw new Exception();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        Artist artist = artistExecuteRequest(artistName);
        JSONObject json = new JSONObject();
        json.put("name",artist.getName());
        json.put("genre",artist.getGenres());
        json.put("followers",artist.getFollowers().toString());
        return json.toJSONString();
    }

    private Artist artistExecuteRequest(String artistName) {

        SearchArtistsRequest searchArtistsRequest = spotifyApi.searchArtists(artistName).limit(5).build();
        Paging<Artist> artistPaging = null;
        try {
            artistPaging  = searchArtistsRequest.execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }
        return artistPaging.getItems()[0];
    }


    public boolean isInputInvalid(String input){
        return (input.contains("select") || input.contains("*"));
    }

}