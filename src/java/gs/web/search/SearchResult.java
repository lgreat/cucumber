package gs.web.search;

import gs.data.search.IndexField;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Explanation;

/**
 * This is a data structure to hold search result values based on lucene
 * <code>Document</code> objects.
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SearchResult {
    private static final Log _log = LogFactory.getLog(SearchResult.class);

    public enum Type {
        distirct,
        school,
        city,
        article,
        term,
        topic
    }

    private String _address;
    Document _doc;
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
        Type type = getType();
        if (type == Type.school) {
            context = getAddress();
        } else if (type == Type.term || type == Type.article || type == Type.topic) {
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
    public Type getType() {
        Type type = null;
        try {
            type = Type.valueOf(_doc.get("type"));
        } catch (Exception e) {
            _log.error("Unknown type in search result", e);
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
            String cityStateZip = getCityStateZip();
            if (StringUtils.isNotBlank(cityStateZip)) {
                addressBuffer.append(",  ");
                addressBuffer.append(cityStateZip);
            }
            _address = addressBuffer.toString ();
        }
        return _address;
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

    public String getStreetAddress() {
        return _doc.get("street");
    }

    public String getCityStateZip() {
        StringBuffer addressBuffer = new StringBuffer();
        String city = _doc.get("city");
        if (StringUtils.isNotBlank(city)) {
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
        return addressBuffer.toString();
    }
}