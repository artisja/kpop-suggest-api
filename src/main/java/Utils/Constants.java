package Utils;

public enum Constants {
    SONG_ID("id"),
    ARTIST_NAME("artistName"),
    LENGTH("length"),
    LIKES("likes"),
    LINK("link"),
    NAME("name");

    public final String attribute;

    Constants(String attribute) {
        this.attribute = attribute;
    }
}
