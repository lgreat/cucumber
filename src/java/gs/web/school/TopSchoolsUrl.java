package gs.web.school;

import gs.data.url.GSUrl;
import gs.data.state.State;

/**
 * @author thuss
 */
public class TopSchoolsUrl extends GSUrl {

    private final static String TOP_HIGH_SCHOOL_PATH = "/top-high-schools/";

    private final String _path;

    public TopSchoolsUrl(State state) {
        StringBuffer uri = new StringBuffer(TOP_HIGH_SCHOOL_PATH);
        if (state != null) uri.append(state.getLongName().toLowerCase().replace(" ", "-")).append("/");
        _path = uri.toString();
    }

    public TopSchoolsUrl() {
        _path = TOP_HIGH_SCHOOL_PATH;
    }

    public String getRelativePath() {
        return _path;
    }
}
