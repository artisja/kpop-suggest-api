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

    String name;
    String link;
    int songId;
    String artistName;
    int length,likes;
//    Artist features;
    ArrayList<Suggest> suggests;

    public Song(){

    }

    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return name;
    }

    @DynamoDBAttribute(attributeName = "link")
    public String getLink() { return link; }

    @DynamoDBAttribute(attributeName = "length")
    public int getLength() {
        return length;
    }

    @DynamoDBAttribute(attributeName = "artistName")
    public String getArtistName() { return artistName; }

    @DynamoDBHashKey(attributeName = "songID")
    public int getSongId() { return songId; }

    @DynamoDBAttribute(attributeName = "likes")
    public int getLikes() { return likes; }

    //    public Artist getFeatures() {
//        return features;
//    }

    @DynamoDBAttribute(attributeName = "suggests")
    public ArrayList<Suggest> getSuggests() {
        return suggests;
    }

   public void setName(String name) {
       this.name = name;
   }

    public void setLikes(int likes) { this.likes = likes; }

    public void setLink(String link) {
        this.link = link;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setSuggests(ArrayList<Suggest> suggests) {
        this.suggests = suggests;
    }

    public void setArtistName(String artistName) { this.artistName = artistName; }

    public void setSongId(int songId) { this.songId = songId; }

    //    public void setFeatures(Artist features) {
//        this.features = features;
//    }
}
