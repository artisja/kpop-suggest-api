package Model;

import com.wrapper.spotify.model_objects.specification.ArtistSimplified;

import java.util.ArrayList;

/**
 * Created by artisja on 4/11/20.
 */
public class Suggest {
    String userId,suggestedToId ,trackLink,songName,comment;
    ArrayList<String> artistIds;

    public ArrayList<String> getArtistIds() {
        return artistIds;
    }

    public String getTrackLink() {
        return trackLink;
    }

    public String getUserId() {
        return userId;
    }

    public String getSuggestedToId() {
        return suggestedToId;
    }

    public String getSongName() {
        return songName;
    }

    public String getComment() {
        return comment;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public void setArtistId(ArrayList<String> artistIds) {
        this.artistIds = artistIds;
    }

    public void setSuggestedToId(String suggestedToId) {
        this.suggestedToId = suggestedToId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTrackLink(String trackLink) {
        this.trackLink = trackLink;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
