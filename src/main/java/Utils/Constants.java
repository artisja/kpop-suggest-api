package Utils;

public enum Constants {
    SONG_ID("songId"),
    ARTIST_NAME("artistName"),
    LENGTH("timeLength"),
    LIKES("likes"),
    LINK("link"),
    NAME("songName"),
    TABLE("songs_table");

    public final String attribute;

    Constants(String attribute) {
        this.attribute = attribute;
    }
}
