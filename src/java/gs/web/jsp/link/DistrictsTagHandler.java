package gs.web.jsp.link;

import gs.data.state.State;
import gs.web.util.UrlBuilder;

/**
 * Privides a link tag handler for the "all districts in state" pages for
 * example: http://www.greatschools.net/schools/districts/California/CA
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class DistrictsTagHandler extends LinkTagHandler {

    private State _state;

    protected UrlBuilder createUrlBuilder() {

        UrlBuilder builder = new UrlBuilder(UrlBuilder.DISTRICTS_PAGE, getState());
        return builder;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }
}
