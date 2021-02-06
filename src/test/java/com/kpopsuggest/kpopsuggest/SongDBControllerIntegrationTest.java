package com.kpopsuggest.kpopsuggest;

import Model.Song;
import Utils.Constants;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemResult;
import com.kpopsuggest.ArtistDBController;
import com.wrapper.spotify.model_objects.specification.Track;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.security.NoSuchAlgorithmException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(MockitoJUnitRunner.class)
public class SongDBControllerIntegrationTest {

    Song song;

    @Mock
    ArtistDBController artistDBController;

    @Mock
    DynamoDB dynamoDB;
    @Before
    public void setUp() {
        song = new Song();
        song.setSongId("1");
        song.setLikes(10);
        song.setArtistName("CL");
        song.setTitle("+5Star+");
        song.setLink("https://www.youtube.com/watch?v=JeGhUESd_1o");
    }

    @Test
    public void testConvertSongNotNull() throws NoSuchAlgorithmException {
        Track track = null;
        when(artistDBController.convertTrackToItem(track)).thenReturn(new TableWriteItems(Constants.TABLE.toString()));
        Assert.notNull(artistDBController.convertTrackToItem(track));
    }

    @Test
    public void testAddSongIsNull() throws NoSuchAlgorithmException {
        Track track = null;
        when(artistDBController.convertTrackToItem(track)).thenReturn(new TableWriteItems(Constants.TABLE.toString()));
        when(dynamoDB.batchWriteItem((TableWriteItems) Mockito.anyObject())).thenReturn(new BatchWriteItemOutcome(new BatchWriteItemResult()));
//        when(songDBController.addSong(Mockito.anyString(),eq(song))).thenReturn(Mockito.anyObject());
        Assert.isNull(artistDBController.addSong("artis"));
    }
}
