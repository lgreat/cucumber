package gs.web.search;


public class SearchException extends Exception {
    public SearchException() {
    }

    public SearchException(String s, Throwable throwable) {
        super(s, throwable);
    }
}