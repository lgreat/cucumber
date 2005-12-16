package gs.web.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Explanation;
import org.apache.commons.lang.StringUtils;
import gs.data.search.highlight.TextHighlighter;
import gs.data.search.IndexField;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SearchResult {

    public final static int DISTRICT = 0;
    public final static int SCHOOL   = 1;
    public final static int CITY     = 2;
    public final static int ARTICLE  = 3;
    public final static int TERM     = 4;

    private String _address;
    private Document _doc;
    private String _query;
    private boolean _highlight = true;
    private Explanation _ex;

    public SearchResult(Document doc) {
        _doc = doc;
    }

    public SearchResult(Document doc, String query) {
        _doc = doc;
        _query = query;
    }

    public String getName() {
        return _doc.get("name");
    }

    public String getId() {
        return _doc.get("id");
    }

    public String getHeadline() {
        String headline = getName();
        if (headline == null) {
            headline = getTitle();
            if (headline == null) {
                headline = getTerm();
                if (headline == null) {
                    headline = getCity();
                }
            }
        }

        if (_highlight) {
            headline = TextHighlighter.highlight(headline, _query, "name"); 
        }
        return headline;
    }

    public String getContext() {
        String context = null;
        String type = _doc.get("type");
        if ("school".equals(type)) {
            context = getAddress();
        } else if ("topic".equals(type) ||
                "article".equals(type) ||
                "term".equals(type)) {
            context = getAbstract();
            if (context == null) {
                context = getDefinition();
            }
        }
        return context;
    }

    public String getGradeLevel() {
        String gradeLevel = "";
        String gl = _doc.get("gradelevel");
        if (!StringUtils.isEmpty(gl)) {
            if ("e".equals(gl)) {
                gradeLevel = "elementary";
            } else if ("m".equals(gl)) {
                gradeLevel = "middle";
            } else if ("h".equals(gl)) {
                gradeLevel = "high";
            }
        }
        return gradeLevel;
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

    public String getAddress() {
        if (_address == null) {
            StringBuffer addressBuffer = new StringBuffer ();
            addressBuffer.append(_doc.get("street"));
            addressBuffer.append(",  ");
            addressBuffer.append(_doc.get("city"));
            addressBuffer.append(", ");
            addressBuffer.append(getState().toUpperCase());
            addressBuffer.append(" ");
            addressBuffer.append(_doc.get("zip"));
            _address = addressBuffer.toString ();
        }

        if (_highlight) {
            _address = TextHighlighter.highlight(_address, _query, "address");
        }

        return _address;
    }

    protected Document getDocument() {
        return _doc;
    }

    public boolean isInsider() {
        return "true".equals(_doc.get("insider"));
    }

    /**
     * @return The 2-letter state abreviation lowercased
     */
    public String getState () {
        return _doc.get("state");
    }

    public String getPhone() {
        return getDocument().get("phone");
    }

    public String getTitle() {
        String title = _doc.get("title");
        if (_highlight) {
               title = TextHighlighter.highlight(title, _query, "title");
        }
        return title;
    }

    public String getCity() {
        return _doc.get("city");
    }

    public String getCityAndState() {
        String cityAndState = _doc.get("citystate");
        if (cityAndState == null || "".equals(cityAndState)) {
            String c = getCity();
            String s = getState();
            if (c != null && s != null) {
                StringBuffer buff = new StringBuffer(c);
                buff.append(", ");
                buff.append(s.toUpperCase());
                cityAndState = buff.toString();
            }
        }
        return cityAndState;
    }

    public String getAbstract() {
        String abs = _doc.get("abstract");
        if (_highlight) {
               abs = TextHighlighter.highlight(abs, _query, "abstract");
        }
        return abs;
    }

    public String getSchoolType() {
        return _doc.get("schooltype");
    }

    /**
     * Turns highlighing on or off.  On by default.
     * @param h
     */
    public void setHighlight(boolean h) {
        _highlight = h;
    }

    public String getTerm() {
        String term = _doc.get("term");
        if (_highlight) {
               term = TextHighlighter.highlight(term, _query, "term");
        }
        return term;
    }

    public String getDefinition() {
        return _doc.get("definition");
    }

    public int getSchools() {
        int count = 0;
        String schools = _doc.get(IndexField.SCHOOLS);
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
        return explanation;
    }
}
