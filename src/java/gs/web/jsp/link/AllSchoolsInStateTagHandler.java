package gs.web.jsp.link;

import gs.data.state.State;
import gs.web.util.UrlBuilder;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class AllSchoolsInStateTagHandler extends LinkTagHandler {
    private State _state;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.SCHOOLS_IN_STATE, _state);
    }

    public void setState(State state) {
        _state = state;
    }
}
