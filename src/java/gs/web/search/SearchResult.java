package gs.web.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Explanation;
import org.apache.commons.lang.StringUtils;
import gs.data.search.IndexField;

/**
 * This is a data structure to hold search result values based on lucene
 * <code>Document</code> objects.
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SearchResult {

    public final static int DISTRICT = 0;
    public final static int SCHOOL   = 1;
    public final static int CITY     = 2;
    public final static int ARTICLE  = 3;
    public final static int TERM     = 4;
    public final static int TOPIC    = 5;

    private String _address;
    private Document _doc;
    private Explanation _ex;

    public SearchResult(Document doc) {
        _doc = doc;
    }

    public String getHeadline() {
        String headline = getName();
        if (headline == null) {
            headline = _doc.get("title");
            if (headline == null) {
                headline = _doc.get("term");
                if (headline == null) {
                    headline = getCity();
                }
            }
        }
        return headline;
    }

    public String getContext() {
        String context = null;
        int t = getType();
        if (t == SCHOOL) {
            context = getAddress();
        } else if (t == TERM || t == ARTICLE || t == TOPIC) {
            context = _doc.get("abstract");
            if (context == null) {
                context = _doc.get("definition");
            }
        }
        return context;
    }

    /**
     * This method returns the type of this result as an int value.  The result
     * types are available as public static members: SCHOOL, DISTRICT, CITY,
     * ARTICLE, TERM.
     * @return an int.
     */
    public int getType() {
        int type = -1; // undefined
        String t = _doc.get("type");
        // t should never be null - NPE if it is.
        if ("district".equals(t)) {
            type = DISTRICT;
        } else if ("school".equals(t)) {
            type = SCHOOL;
        } else if ("city".equals(t)) {
            type = CITY;
        } else if ("article".equals(t)) {
            type = ARTICLE;
        } else if ("term".equals(t)) {
            type = TERM;
        }
        return type;
    }

    protected String getAddress() {
        if (_address == null) {
            StringBuffer addressBuffer = new StringBuffer ();
            String street = _doc.get("street");
            if (StringUtils.isNotBlank(street)) {
                addressBuffer.append(street);
            }
            String city = _doc.get("city");
            if (StringUtils.isNotBlank(city)) {
                addressBuffer.append(",  ");
                addressBuffer.append(city);
            }

            String state = getState().toUpperCase();
            if (StringUtils.isNotBlank(state)) {
                addressBuffer.append(", ");
                addressBuffer.append(state);
            }

            String zip = _doc.get("zip");
            if (StringUtils.isNotBlank(zip)) {
                addressBuffer.append(" ");
                addressBuffer.append(zip);
            }
            _address = addressBuffer.toString ();
        }
        return _address;
    }

    public boolean isInsider() {
        return "true".equals(_doc.get("insider"));
    }

    /**
     * @return The 2-letter state abreviation lowercased
     */
    public String getState () {
        String state = _doc.get("state");
        return (state != null) ? state : "";
    }

    public String getName() {
        return _doc.get("name");
    }

    public String getId() {
        return _doc.get("id");
    }

    public String getCity() {
        return _doc.get("city");
    }

    public String getSchoolType() {
        return _doc.get("schooltype");
    }

    public int getSchools() {
        int count = 0;
        String schools = _doc.get(IndexField.NUMBER_OF_SCHOOLS);
        if (schools != null) {
            count = Integer.parseInt(schools);
        }
        return count;
    }

    public void setExplanation(Explanation ex) {
        _ex = ex;
    }

    public String getExplanation() {
        String explanation = "";
        if (_ex != null) {
            explanation = _ex.toString();
        }
        return explanation.trim();
    }
}