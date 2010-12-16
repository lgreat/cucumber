package gs.web.search;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;

public abstract class BaseLuceneSearchService {
    protected static final String PUNCTUATION_AND_WHITESPACE_PATTERN = "^[\\p{Punct}\\s]*$";

    static String padCommasAndNormalizeExtraSpaces(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll(",", ", ").replaceAll("\\s+", " ");
    }

    public String cleanseSearchString(String searchString) {
        searchString = StringUtils.trimToNull(searchString);
        searchString = StringUtils.lowerCase(searchString);

        if (searchString != null && searchString.matches(PUNCTUATION_AND_WHITESPACE_PATTERN)) {
            return null;//TODO: throw exception instead?
        }

        searchString = padCommasAndNormalizeExtraSpaces(searchString);

        if (searchString != null) {
            searchString = QueryParser.escape(searchString);
        }

        searchString = StringUtils.trimToNull(searchString);

        return searchString;
    }
}
