package Utils;

import Model.Song;

import java.math.BigDecimal;
import java.util.Map;

public class SongDBUtil {

    public SongDBUtil(){
    }

    public Song transferItem(Iterable<java.util.Map.Entry<String, Object>> map,Song song) {
        BigDecimal intConverter = new BigDecimal(0);
        for (Map.Entry<String,Object> o  : map) {
            switch (o.getKey()){
                case "artistName": song.setArtistName(o.getValue().toString());
                    break;
                case "length":  intConverter = (BigDecimal) o.getValue();
                               song.setTimeLength(intConverter.intValue());
                case "link": song.setLink(o.getValue().toString());
                    break;
                case "name": song.setTitle(o.getValue().toString());
                    break;
                case "likes": intConverter = (BigDecimal) o.getValue();
                    song.setLikes(intConverter.intValue());
                    break;
                case "songId": intConverter = (BigDecimal) o.getValue();
                    song.setSongId(intConverter.intValue());
                    break;
            }
        }
       return song;
    }
}
