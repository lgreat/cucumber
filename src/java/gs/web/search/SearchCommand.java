package gs.web.search;

/**
 * @author Chris Kimm <mailto:chris@seeqa.com>
 */
public class SearchCommand {

    private String _query;
    private String _constraint;
    private String _type;
    private String _q;

    public void setQ(String q) {
        _q = q;
    }

    public String getQ() {
        return _q;
    }
    
    public String getQuery() {
        return _query;
    }

    public void setQuery(String _query) {
        this._query = _query;
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

    public void setType(String _type) {
        this._type = _type;
    }
}
