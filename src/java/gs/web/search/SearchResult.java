package gs.web.search;

import org.apache.lucene.document.Document;
import gs.data.search.highlight.TextHighlighter;
import gs.data.search.IndexField;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SearchResult {

    private String _address;
    private Document _doc;
    private String _query;
    private boolean _highlight = true;

    public SearchResult(Document doc) {
        _doc = doc;
    }

    public SearchResult(Document doc, String query) {
        _doc = doc;
        _query = query;
    }

    public String getName() {
        String name = _doc.get("name");
        if (_highlight) {
            name = TextHighlighter.highlight(name, _query, "name");
        }
        return name;
    }

    public String getId() {
        return _doc.get("id");
    }

    public String getAddress() {
        if (_address == null) {
            StringBuffer addressBuffer = new StringBuffer ();
            addressBuffer.append(_doc.get("street"));
            addressBuffer.append("  ");
            addressBuffer.append(_doc.get("city"));
            addressBuffer.append(", ");
            addressBuffer.append(getState());
            addressBuffer.append(" ");
            addressBuffer.append(_doc.get("zip"));
            _address = addressBuffer.toString ();
        }

        if (_highlight) {
            return TextHighlighter.highlight(_address, _query, "address");
        }

        return _address;
    }

    protected Document getDocument() {
        return _doc;
    }


    public String getInsider() {
        String isInsider = "false";
        String insider = _doc.get("insider");
        if (insider != null) {
            if (insider.equals("true")) {
                isInsider = "true";
            }
        }
        return isInsider;
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

    public String getCityName() {
        //return _doc.get("cityname");
        String city = _doc.get("cityname");
        if (_highlight) {
               city = TextHighlighter.highlight(city, _query, "cityname");
        }
        return city;
    }

    public String getCityAndState() {
        //return _doc.get("citystate");
        String cityAndState = _doc.get("citystate");
        if (_highlight) {
               cityAndState = TextHighlighter.highlight(cityAndState, _query, "citystate");
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
        //return _doc.get("term");
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
}
