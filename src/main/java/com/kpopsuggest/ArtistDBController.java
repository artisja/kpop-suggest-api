package com.kpopsuggest;

import Model.Playlist;
import Model.Song;
import Model.SongIDWrapper;
import Model.Suggest;
import Utils.Constants;
import Utils.SongDBUtil;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.i18n.CountryCode;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistsTopTracksRequest;
import com.wrapper.spotify.requests.data.playlists.CreatePlaylistRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchArtistsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import javafx.util.Pair;
import net.minidev.json.JSONObject;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;

import com.wrapper.spotify.SpotifyApi;

import javax.annotation.PostConstruct;
import javax.ws.rs.PathParam;

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
    private final Table suggestTable = dynamoDB.getTable(Constants.TABLE.attribute);
    private final Index index = suggestTable.getIndex("suggestedToId-index");

    @GetMapping("/RedVelvet")
    public String redVelvetTest(@RequestParam(value = "song", defaultValue = "Happiness") String song) {
        return String.format("Hello %s!", song);
    }

    @GetMapping(path = "/Suggestion/{suggestedToId}")
    public ResponseEntity<JSONObject> getUserSuggests(@PathVariable("suggestedToId") String suggestedToId) {
        ResponseEntity<JSONObject> response = null;
        JSONObject returnJson = new JSONObject();
        if(isInputInvalid(suggestedToId)) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("User Credentials Issue", "Invalid User: " + suggestedToId);
            response = new ResponseEntity<JSONObject>(jsonObject,HttpStatus.NOT_ACCEPTABLE);
            return response;
        }
        try{
            //dynamo db search call
            QuerySpec suggestQuerySpec = new QuerySpec()
                    .withKeyConditionExpression("suggestedToId = :s_id")
                    .withValueMap(new ValueMap()
                            .withString(":s_id", suggestedToId));
            suggestQuerySpec.setMaxResultSize(2);
            ItemCollection<QueryOutcome> suggestQueryItems = index.query(suggestQuerySpec);
            System.out.println(suggestQueryItems.firstPage().toString());
//            //add sqs queue to publish to pull down in refresh?
            List<String> suggestList = new ArrayList<String>();
            suggestQueryItems.firstPage();
            suggestQueryItems.iterator().next().toString();
            Iterator<Item> itemIterator = suggestQueryItems.iterator();
            while (itemIterator.hasNext()) {
                Item nextItem = itemIterator.next();
                suggestList.add(nextItem.toJSONPretty().toString());
            }
            returnJson.put("Results", suggestList.toString());
            System.out.println(suggestQueryItems.getLastLowLevelResult().getItems().get(0));
            returnJson.put("LastItem",suggestQueryItems.getLastLowLevelResult().getItems().get(0).get("LastEvaluatedKey"));
        }catch (Exception exception){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Amazon Dynamo DB Issue: ",exception.getMessage());
            return new ResponseEntity<JSONObject>(jsonObject,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        response = new ResponseEntity<>(returnJson,HttpStatus.ACCEPTED);
        return response;
    }


    /**
     *
     * @param suggestion
     * @return Response entity
     */
    @PutMapping(path = "/Suggestion", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<JSONObject> addSuggest(@RequestBody Suggest suggestion) {
        Paging<Track>  trackPaging = null;
        try {
            trackPaging = searchSpotifyForSong(suggestion);
        } catch (Exception exception) {
            exception.printStackTrace();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Spotify API Issue",exception.getMessage());
            return new ResponseEntity<JSONObject>(jsonObject,HttpStatus.INTERNAL_SERVER_ERROR);
        }

        BatchWriteItemOutcome batchWriteItemOutcome = null;
        Track topTrack = trackPaging.getItems()[0];
        suggestion.setTrackLink(topTrack.getUri());
        Stream<ArtistSimplified> artistSimplifiedStream = Arrays.stream(topTrack.getArtists());
        ArrayList<String> artistList = new ArrayList<String>();
        artistSimplifiedStream.forEach(artist -> artistList.add(artist.getId()));
        suggestion.setArtistId(artistList);

        try {
            batchWriteItemOutcome = dynamoDB.batchWriteItem(convertTrackToSuggest(suggestion));
        } catch (NoSuchAlgorithmException e) {
            JSONObject json = new JSONObject();
            json.put("Result","Duplicate Song");
            return new ResponseEntity<JSONObject>(json, HttpStatus.CONFLICT);
        }
        JSONObject json = new JSONObject();
        json.put("Result", batchWriteItemOutcome.getBatchWriteItemResult().toString());
        json.put("Link",suggestion.getTrackLink());
        return new ResponseEntity<JSONObject>(json, HttpStatus.CREATED);
    }

    private TableWriteItems convertTrackToSuggest(Suggest suggestion) throws NoSuchAlgorithmException {
        return new TableWriteItems(Constants.TABLE.attribute)
                .withItemsToPut(
                        new Item()
                                .withPrimaryKey(Constants.SUGGESTION_ID.attribute, UUID.randomUUID().toString())
                                .withString(Constants.USER_ID.attribute,suggestion.getUserId())
                                .withString(Constants.TRACK_URL.attribute,suggestion.getTrackLink())
                                .withList(Constants.ARTIST_ID.attribute,suggestion.getArtistIds())
                                .withString(Constants.SUGGESTTO_ID.attribute, suggestion.getSuggestedToId())
                                .withString(Constants.NAME.attribute,suggestion.getSongName())
                                .withString(Constants.COMMENT.attribute,(suggestion.getComment()==null || suggestion.getComment().isEmpty()) ? Constants.DEFAULT_COMMENT_MESSAGE.attribute:suggestion.getComment())
                );
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
    

//    private Item isSongCreated(String songName) {
//       Item songItem = songTable.getItem(new PrimaryKey(Constants.SONG_ID.attribute,songName));
//       return songItem;
//    }

    private Paging<Track> searchSpotifyForSong(Suggest suggest) throws Exception {
        SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(suggest.getSongName() + Constants.SEARCH_KEY.attribute).build();
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

    @PostMapping(path = "/playlist/{playlistName}", produces = "application/json")
    public ResponseEntity<JSONObject> createPlaylist(@PathParam("playlistName") String playlistName, @RequestBody Playlist playlist) {
        return new ResponseEntity<JSONObject>(HttpStatus.ACCEPTED);
    }

    private String createPlaylistURL(String userId,String suggestToId,String playlistName) {
        String playlistURL = "";
        CreatePlaylistRequest createPlaylistRequest = spotifyApi.createPlaylist(userId, playlistName)
          .collaborative(true)
          .public_(false)
          .description("Amazing music.").build();

        return playlistURL;
    }

    private Track[] searchTopTracks(String artistName) throws Exception {
        Artist artist = artistExecuteRequest(artistName);

        GetArtistsTopTracksRequest getArtistsTopTracksRequest = spotifyApi.getArtistsTopTracks(artist.getId(),CountryCode.US).build();
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
        return artistTracks;
    }

    @GetMapping(path = "search/artists/{artist}/songs",produces = "application/json")
    @ResponseStatus(HttpStatus.FOUND)
    public  ResponseEntity<JSONObject> searchArtistSongs(@PathVariable("artist") String artistName) {
        JSONObject searchResultJson = new JSONObject();
        if(isInputInvalid(artistName)){
            searchResultJson.put("Error Message", "Invalid Artist Name");
            return new ResponseEntity<JSONObject>(searchResultJson, HttpStatus.BAD_REQUEST);
        }

        Track [] artistTracks = new Track[0];
        try {
            artistTracks = searchTopTracks(artistName);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        if(artistTracks.length==0){
            return new ResponseEntity<JSONObject>(searchResultJson, HttpStatus.NOT_FOUND);
        }

        //process artist tracks for less info in Json
        searchResultJson.put("Artist Track Results", artistTracks);
        searchResultJson.put("Status", HttpStatus.FOUND);
        searchResultJson.put("link","/Suggestion" );
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