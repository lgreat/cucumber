package gs.web.search;
import org.apache.lucene.document.Document;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolResult {

    private String _address;
    private Document _doc;

    public SchoolResult(Document doc) {
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
            addressBuffer.append(_doc.get("state"));
            addressBuffer.append(" ");
            addressBuffer.append(_doc.get("zip"));
            _address = addressBuffer.toString ();
        }
        return _address;
    }
}
