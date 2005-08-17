package gs.web.search;

import org.apache.lucene.document.Document;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SearchResult {

    private String _address;
    private Document _doc;

    public SearchResult(Document doc) {
        _doc = doc;
    }

    public String getName() {
        return _doc.get("name");
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
        return _doc.get("title");
    }

    public String getAbstract() {
        return _doc.get("abstract");
    }

}
