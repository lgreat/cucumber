package gs.web.jsp.link;

import gs.data.state.State;
import gs.web.util.UrlBuilder;

public class SchoolChoiceCenterTagHandler extends LinkTagHandler {
    private State _state;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.SCHOOL_CHOICE_CENTER, _state);
    }

    public void setState(State state) {
        _state = state;
    }
}
