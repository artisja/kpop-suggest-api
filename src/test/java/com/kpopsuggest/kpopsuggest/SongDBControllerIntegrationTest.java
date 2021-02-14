package com.kpopsuggest.kpopsuggest;

import Model.Song;
import Utils.Constants;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemResult;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.amazonaws.services.licensemanager.model.LicenseConfigurationStatus;
import com.kpopsuggest.ArtistDBController;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import org.apache.hc.core5.http.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import javax.inject.Inject;
import javax.validation.constraints.Max;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(MockitoJUnitRunner.class)
public class SongDBControllerIntegrationTest {

    Song song;

    @Value("${client.id}")
    private String clientId;

    @Value("${client.secret}")
    private String clientSecret;

    SpotifyApi spotifyApi;

    @Mock
    DynamoDB dynamoDB;


    @InjectMocks
    ArtistDBController artistDBController;

    @Before
    public void setUp() {
        song = new Song();
        song.setSongId("1");
        song.setLikes(10);
        song.setArtistName("CL");
        song.setTitle("+5Star+");
        song.setLink("https://www.youtube.com/watch?v=JeGhUESd_1o");
    }


//    @Test
//    public void testAddSongIsNull() {
//        when(dynamoDB.batchWriteItem((TableWriteItems) Mockito.anyObject())).thenReturn(new BatchWriteItemOutcome(new BatchWriteItemResult()));
//        Assert.isNull(artistDBController.addSong("artis"));
//    }

//    @Test
//    public void testAddSong(){
//        BatchWriteItemResult batchWriteItemResult = new BatchWriteItemResult();
//        List<WriteRequest> writeRequestList = new ArrayList<WriteRequest>();
//        writeRequestList.add(new WriteRequest());
//        when(spotifyApi.searchTracks("temp")).thenReturn(new SearchTracksRequest.Builder(Mockito.anyString()));
//        batchWriteItemResult.addUnprocessedItemsEntry("CL",writeRequestList);
//        when(dynamoDB.batchWriteItem((TableWriteItems) Mockito.anyObject())).thenReturn(new BatchWriteItemOutcome(batchWriteItemResult));
//        String result = artistDBController.addSong("Baddest Female");
//        Assert.notNull(result);
//    }
}
