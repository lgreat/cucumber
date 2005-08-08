package gs.web.search;

import org.apache.lucene.document.Document;

/**
 * Created by IntelliJ IDEA.
 * User: Bishop
 * Date: Aug 6, 2005
 * Time: 9:49:44 AM
 * // todo: this is a temporary class - to be replace in ResultPager by Dao implementation ck.
 */
public class ArticleResult {

    private Document _doc;

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
}
