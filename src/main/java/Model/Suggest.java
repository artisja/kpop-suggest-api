package java.Model;

import java.Model.Artist;
import java.Model.Song;

/**
 * Created by artisja on 4/11/20.
 */
public class Suggest {
    String user,name;
    Song song;
    Artist artist;


    public String getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public Song getSong() {
        return song;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }
}
