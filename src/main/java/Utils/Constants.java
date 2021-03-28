package Utils;

public enum Constants {
    TRACK_URL("trackUrl"),
    ARTIST_ID("artistId"),
    USER_ID("userId"),
    SUGGESTION_ID("suggestId"),
    SUGGESTTO_ID("suggestedToId"),
    NAME("trackName"),
    COMMENT("comment"),
    DEFAULT_COMMENT_MESSAGE("You got a recommended song."),
    TABLE("suggestions_table");

    public final String attribute;

    Constants(String attribute) {
        this.attribute = attribute;
    }
}
