package gs.web.jsp.link;

import gs.data.state.State;
import gs.web.util.UrlBuilder;

public class Cities2TagHandler extends LinkTagHandler {
    private State _state;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.CITIES2, _state);
    }

    public void setState(State state) {
        _state = state;
    }
}
