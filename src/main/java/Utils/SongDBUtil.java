package Utils;

import Model.Song;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;

public class SongDBUtil {

    public SongDBUtil(){
    }

    public Song transferItem(Iterator<Map.Entry<String, Object>> map, Song song) {
        BigDecimal intConverter = new BigDecimal(0);
        while (map.hasNext()) {
            Map.Entry songObject = map.next();
            switch (songObject.getKey().toString()){
                case "artistName": song.setArtistName(songObject.getValue().toString());
                    break;
                case "length": intConverter = (BigDecimal) songObject.getValue();
                               song.setTimeLength(intConverter.intValue());
                case "link": song.setLink(songObject.getValue().toString());
                    break;
                case "title": song.setTitle(songObject.getValue().toString());
                    break;
                case "likes": intConverter = (BigDecimal) songObject.getValue();
                    song.setLikes(intConverter.intValue());
                    break;
                case "songId": intConverter = (BigDecimal) songObject.getValue();
                    song.setSongId(intConverter.intValue());
                    break;
            }
        }
       return song;
    }
}
