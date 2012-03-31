package gs.web.search;


import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.SpellCheckResponse;

import java.util.Map;

/**
 * Contains methods for managing search spellcheck suggestions, a.k.a "Did You Mean"
 */
public class SpellChecking {

    public static String getSearchSuggestion(String searchString, SpellCheckResponse spellCheckResponse) {
        String suggestedSearch = searchString;

        Map<String,SpellCheckResponse.Suggestion> suggestionMap = spellCheckResponse.getSuggestionMap();

        String[] tokens = StringUtils.splitPreserveAllTokens(suggestedSearch);

        if (tokens != null) {
            for (int i = 0; i < tokens.length; i++) {
                if (suggestionMap.containsKey(tokens[i])) {
                    tokens[i] = suggestionMap.get(tokens[i]).getAlternatives().get(0);
                } else if (suggestionMap.containsKey(StringUtils.lowerCase(tokens[i]))) {
                    tokens[i] = suggestionMap.get(StringUtils.lowerCase(tokens[i])).getAlternatives().get(0);
                }
            }
        }

        String result = StringUtils.join(tokens, ' ');

        if (searchString.equalsIgnoreCase(result)) {
            result = null;
        }

        return result;
    }

}
