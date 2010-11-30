package gs.web.search;

public abstract class BaseLuceneSearchService {
    protected static final String PUNCTUATION_AND_WHITESPACE_PATTERN = "^[\\p{Punct}\\s]*$";

    static String padCommasAndNormalizeExtraSpaces(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll(",", ", ").replaceAll("\\s+", " ");
    }
}
