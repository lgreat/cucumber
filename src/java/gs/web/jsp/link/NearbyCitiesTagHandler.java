package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.state.State;

public class NearbyCitiesTagHandler extends LinkTagHandler {
    private String _city;
    private State _state;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.CITIES_MORE_NEARBY, _state, _city);
    }

    public String getCity() {
        return _city;
    }

    public void setCity(String city) {
        _city = city;
    }


    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }
}
