package Model;

/**
 * Created by artisja on 4/11/20.
 */
public class Suggest {
    String userId,suggestedToId, artistId,trackLink;;

    public String getArtistId() {
        return artistId;
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

    public void setArtistId(String artistId) {
        this.artistId = artistId;
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
}
