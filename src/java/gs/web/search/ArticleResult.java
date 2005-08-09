package gs.web.search;

import org.apache.lucene.document.Document;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class ArticleResult {

    private Document _doc;
    private static final Log _log = LogFactory.getLog(ArticleResult.class);

    public ArticleResult(Document doc) {
        _doc = doc;
    }

    public String getTitle() {
        return _doc.get("title");
    }

    public String getId() {
        return _doc.get("id");
    }

    public String getAbstract() {
        return _doc.get("abstract");
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
}
