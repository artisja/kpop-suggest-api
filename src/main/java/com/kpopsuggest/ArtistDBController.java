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
import com.google.gson.JsonObject;
import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistsTopTracksRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchArtistsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import javafx.util.Pair;
import net.minidev.json.JSONObject;
import org.apache.hc.core5.http.ParseException;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;

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
    private DynamoDB dynamoDB = new DynamoDB(client);
    private final Table songTable = dynamoDB.getTable(Constants.TABLE.attribute);


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
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<JSONObject> addSong(@PathVariable("songName") String songName) {
        Paging<Track>  trackPaging = null;
        try {
            trackPaging = searchSpotifyForSong(songName);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        BatchWriteItemOutcome batchWriteItemOutcome = null;
        Track topTrack = trackPaging.getItems()[0];
        try {
            batchWriteItemOutcome = dynamoDB.batchWriteItem(convertTrackToItem(topTrack));
        } catch (NoSuchAlgorithmException e) {
            JSONObject json = new JSONObject();
            json.put("Result","Duplicate Song");
            return new ResponseEntity<JSONObject>(json, HttpStatus.CONFLICT);
        }
        JSONObject json = new JSONObject();
        json.put("Result", batchWriteItemOutcome.getBatchWriteItemResult().toString());
        json.put("link","/Song/"+topTrack.getId());
        return new ResponseEntity<JSONObject>(json, HttpStatus.CREATED);
    }

    private TableWriteItems convertTrackToItem(Track track) throws NoSuchAlgorithmException{
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

    /**
     *
     * @param song
     * @param songId
     * @return
     */
    @PutMapping(path = "/Songs/update/{songId}",consumes = "application/json",produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<JSONObject> updateSong(@RequestBody Song song,@PathVariable String songId){
        Map<String, Object> resultUpdate = null;
        JSONObject resultJson = null;
        Pair<String,ValueMap> expressionUpdatePair = buildUpdateExpression(song);

        UpdateItemSpec updateSongSpec = new UpdateItemSpec().withPrimaryKey("songId",songId)
                .withUpdateExpression(expressionUpdatePair.getKey())
                .withValueMap(expressionUpdatePair.getValue())
                .withReturnValues(ReturnValue.UPDATED_NEW);
        try{
            UpdateItemOutcome updateSongOutcome = songTable.updateItem(updateSongSpec);
            resultUpdate = updateSongOutcome.getItem().asMap();
            resultJson = new JSONObject(resultUpdate);
        }catch(Exception exception){
            System.err.println(exception);
            System.err.println(exception.toString());
        }
        return new ResponseEntity<JSONObject>(resultJson,HttpStatus.ACCEPTED);
    }

    private Pair<String, ValueMap> buildUpdateExpression(Song song) {
        StringBuilder updateExpressionBuilder = new StringBuilder();
        ValueMap expressionValueMap = new ValueMap();
        updateExpressionBuilder.append("set ");
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> props = mapper.convertValue(song, Map.class);
        props.values().removeIf(propVal -> propVal == null || propVal.toString().equals(0));
        for(String propsKeys :props.keySet()) {
            switch (propsKeys){
                case "artistName": updateExpressionBuilder.append("artistName = :a,");
                    expressionValueMap.withString(":a",song.getArtistName());
                    break;
                case "length": updateExpressionBuilder.append("timeLength = :l,");
                    expressionValueMap.withInt(":l",song.getTimeLength());
                    break;
                case "link": updateExpressionBuilder.append("link = :n,");
                    expressionValueMap.withString(":n",song.getLink());
                    break;
                case "songName": updateExpressionBuilder.append("songName = :t,");
                    expressionValueMap.withString(":t", song.getTitle());
                    break;
                case "likes": updateExpressionBuilder.append("likes = :k,");
                    expressionValueMap.withInt(":k", song.getLikes());
                    break;
            }
        }
        updateExpressionBuilder.deleteCharAt(updateExpressionBuilder.lastIndexOf(","));
       return new Pair<String, ValueMap>(updateExpressionBuilder.toString(),expressionValueMap);
    }

    /**
     *
     * Get Song
     *
     * Will comment out as not needed as of now
     * @param songName
     * @return Json
     */
//    @GetMapping(value = "/Song/{songName}",produces = "application/json")
//    @ResponseStatus(HttpStatus.FOUND)
//    public String getSong(@PathVariable("songName") String songName) {
//        //check database for song if not there then will need to send to spotify to retrieve
//        String response = null;
//        Item songitem = isSongCreated(songName);
//        if(!songitem.isNull(Constants.SONG_ID.attribute)){
//            response = songitem.toJSONPretty();
//           return response;
//        }else{
//            Paging<Track> trackPaging = null;
//            try {
//                trackPaging = searchSpotifyForSong(songName);
//                Track song = trackPaging.getItems()[0];
//                ObjectMapper objectMapper = new ObjectMapper();
//                response = objectMapper.writeValueAsString(song);
//            } catch (Exception exception) {
//                exception.printStackTrace();
//            }
//        }
//        return response;
//    }

    private Item isSongCreated(String songName) {
       Item songItem = songTable.getItem(new PrimaryKey(Constants.SONG_ID.attribute,songName));
       return songItem;
    }

    private Paging<Track> searchSpotifyForSong(String songName) throws Exception {
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

    @GetMapping(path = "search/artists/{artist}/songs",produces = "application/json")
    @ResponseStatus(HttpStatus.FOUND)
    public  ResponseEntity<JSONObject> searchArtistSongs(@PathVariable("artist") String artistName) {
        JSONObject searchResultJson = new JSONObject();
        if(isInputInvalid(artistName)){
            searchResultJson.put("Error Message", "Invalid Artist Name");
            return new ResponseEntity<JSONObject>(searchResultJson, HttpStatus.BAD_REQUEST);
        }

        GetArtistsTopTracksRequest getArtistsTopTracksRequest = spotifyApi.getArtistsTopTracks(artistName, CountryCode.KR).build();
        Track[] artistTracks = new Track[0];
        try {
            artistTracks = getArtistsTopTracksRequest.execute();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (SpotifyWebApiException spotifyWebApiException) {
            spotifyWebApiException.printStackTrace();
        } catch (ParseException parseException) {
            parseException.printStackTrace();
        }
        searchResultJson.put("Artist Track Results", artistTracks);
        searchResultJson.put("Status", HttpStatus.FOUND);
        return new ResponseEntity<JSONObject>(searchResultJson, HttpStatus.FOUND);
    }

    /**
     *
     * @param artistName
     * @return
     */
    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping(path = "/search/artist/{artist}",consumes = "application/json",produces = "application/json")
    public ResponseEntity<JSONObject> searchArtist(@PathVariable("artist") String artistName) {
        JSONObject searchJson = new JSONObject();
        if(isInputInvalid(artistName)){
            try {
                throw new Exception();
            } catch (Exception exception) {
                exception.printStackTrace();
                searchJson.put("Error Message", "Invalid Artist Name");
                return new ResponseEntity<JSONObject>(searchJson, HttpStatus.BAD_REQUEST);
            }
        }
        Artist artist = artistExecuteRequest(artistName);

        searchJson.put("name",artist.getName());
        searchJson.put("genre",artist.getGenres());
        searchJson.put("followers",artist.getFollowers().toString());
        return new ResponseEntity<JSONObject>(searchJson, HttpStatus.FOUND);
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


    private boolean isInputInvalid(String input){
        return (input.contains("select") || input.contains("*") || input.isEmpty() || input == null);
    }

}