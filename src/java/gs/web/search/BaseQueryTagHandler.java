package gs.web.search;

import gs.web.jsp.BaseTagHandler;
import gs.data.state.State;
import org.apache.taglibs.standard.functions.Functions;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public abstract class BaseQueryTagHandler extends BaseTagHandler {

    protected String _query = "";

    public void setQuery(String q) {
        _query = Functions.escapeXml(q);
    }

    /**
     * @return A string with the state appended to the query as a uri parameter
     */
    protected String getDecoratedQuery() {
        String decoQuery = _query;
        State s = getState();
        if (s != null) {
            StringBuffer buff = new StringBuffer(Functions.escapeXml(_query));
            buff.append("&state=");
            buff.append(s.getAbbreviationLowerCase());
            decoQuery = buff.toString();
        }
        return decoQuery;
    }
}
