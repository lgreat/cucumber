package gs.web.search;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Filter;
import gs.data.search.GSQueryParser;

/**
 * @author Chris Kimm <mailto:chris@seeqa.com>
 */
public class SearchCommand {

    private String _type;
    private String _q;
    private int page;
    private String _state;
    private String _schooltype;
    private String _gradelevel;
    private Filter _filter;

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

    public String getType() {
        return _type;
    }

    public void setSchoolType(String type) {
        _schooltype = type;
    }

    public String getSchoolType() {
        return _schooltype;
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
        _state = s;
    }

    public String getState() {
        return _state;
    }

    public Filter getFilter() {
        return _filter;
    }
}
