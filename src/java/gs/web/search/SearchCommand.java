package gs.web.search;

import org.apache.lucene.search.Query;
import gs.data.search.GSQueryParser;

/**
 * @author Chris Kimm <mailto:chris@seeqa.com>
 */
public class SearchCommand {

    private String _constraint;
    private String _type;
    private String _q;
    private int page;
    private String state;

    public void setQ(String q) {
        _q = q;
    }

    public Query getQuery () throws Exception {
        Query query = null;
        if (_q != null) {
            query = GSQueryParser.parse(_q);
        } else {
        // try to build the query using the other parameters

        }
        return query;
    }

    public String getConstraint() {
        return _constraint;
    }

    public void setConstraint(String _constraint) {
        this._constraint = _constraint;
    }

    public String getType() {
        return _type;
    }

    public void setC(String _type) {
        this._type = _type;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int p) {
        page = p;
    }

    public void setState(String s) {
        state = s;
    }

    public String getState() {
        return state;
    }
}
