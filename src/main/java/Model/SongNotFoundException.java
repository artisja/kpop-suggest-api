package Model;

public class SongNotFoundException extends Exception{

    public SongNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
