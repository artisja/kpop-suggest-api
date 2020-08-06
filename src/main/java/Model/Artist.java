package java.Model;

import java.util.ArrayList;

/**
 * Created by artisja on 4/11/20.
 */
public class Artist {

    String name;
    ArrayList<Song> songList;
    ArrayList<String> followers;
    ArrayList<Suggest> suggests;
    int likes;

    public String getName() {
        return name;
    }

    public ArrayList<Song> getSongList() {
        return songList;
    }

    public ArrayList<String> getFollowers() {
        return followers;
    }

    public ArrayList<Suggest> getSuggests() {
        return suggests;
    }

    public int getLikes() {
        return likes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSongList(ArrayList<Song> songList) {
        this.songList = songList;
    }

    public void setFollowers(ArrayList<String> followers) {
        this.followers = followers;
    }

    public void setSuggests(ArrayList<Suggest> suggests) {
        this.suggests = suggests;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
}
