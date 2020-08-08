package Model;

import java.util.ArrayList;

/**
 * Created by artisja on 4/11/20.
 */
public class Song {

    String name,link,songId;
    int length;
    Artist features;
    ArrayList<Suggest> suggests;
    byte[] file;

    public Song(){

    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public int getLength() {
        return length;
    }

    public Artist getFeatures() {
        return features;
    }

    public ArrayList<Suggest> getSuggests() {
        return suggests;
    }

    public byte[] getFile() {
        return file;
    }

   public void setName(String name) {
       this.name = name;
   }

    public void setLink(String link) {
        this.link = link;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setSuggests(ArrayList<Suggest> suggests) {
        this.suggests = suggests;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public void setFeatures(Artist features) {
        this.features = features;
    }
}
