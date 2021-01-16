package Model;

import java.util.ArrayList;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;


/**
 * Created by artisja on 4/11/20.
 */
@DynamoDBTable(tableName="song_table")
public class Song {

    String title;
    String link;
    String songId;
    String artistName;
    int timeLength,likes;
//    Artist features;
    ArrayList<Suggest> suggests;

    public Song(){

    }

    @DynamoDBAttribute(attributeName = "name")
    public String getTitle() {
        return title;
    }

    @DynamoDBAttribute(attributeName = "link")
    public String getLink() { return link; }

    @DynamoDBAttribute(attributeName = "length")
    public int getTimeLength() {
        return timeLength;
    }

    @DynamoDBAttribute(attributeName = "artistName")
    public String getArtistName() { return artistName; }

    @DynamoDBHashKey(attributeName = "songId")
    public String getSongId() { return songId; }

    @DynamoDBAttribute(attributeName = "likes")
    public int getLikes() { return likes; }

    //    public Artist getFeatures() {
//        return features;
//    }

    @DynamoDBAttribute(attributeName = "suggests")
    public ArrayList<Suggest> getSuggests() {
        return suggests;
    }

   public void setTitle(String title) {
       this.title = title;
   }

    public void setLikes(int likes) { this.likes = likes; }

    public void setLink(String link) {
        this.link = link;
    }

    public void setTimeLength(int timeLength) {
        this.timeLength = timeLength;
    }

    public void setSuggests(ArrayList<Suggest> suggests) {
        this.suggests = suggests;
    }

    public void setArtistName(String artistName) { this.artistName = artistName; }

    public void setSongId(String songId) { this.songId = songId; }

    //    public void setFeatures(Artist features) {
//        this.features = features;
//    }
}
