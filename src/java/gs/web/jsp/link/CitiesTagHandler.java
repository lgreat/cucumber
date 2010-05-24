package gs.web.jsp.link;

import gs.data.state.State;
import gs.web.util.UrlBuilder;

public class CitiesTagHandler extends LinkTagHandler {
    private State _state;
    private String _page;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.CITIES, _state, _page);
    }

    public void setState(State state) {
        _state = state;
    }

    public void setPage(String page) {
        _page = page;
    }
}
