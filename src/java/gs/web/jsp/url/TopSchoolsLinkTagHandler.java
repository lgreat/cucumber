package gs.web.jsp.url;

import gs.data.url.GSUrl;
import gs.data.state.State;
import gs.web.school.TopSchoolsUrl;

/**
 * @author thuss
 */
public class TopSchoolsLinkTagHandler extends LinkTagHandler {
    private State _state;

    protected GSUrl createGSUrl() {
        return new TopSchoolsUrl(_state);
    }

    public void setState(State state) {
        _state = state;
    }
}
