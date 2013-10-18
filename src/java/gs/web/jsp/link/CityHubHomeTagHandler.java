package gs.web.jsp.link;

import gs.data.state.State;
import gs.web.util.UrlBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 10/18/13
 * Time: 2:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class CityHubHomeTagHandler extends LinkTagHandler {
    private State _state;
    private String _city;

    @Override
    public UrlBuilder createUrlBuilder() {
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.CITY_PAGE, getState(), getCity());
        return urlBuilder;
    }

    public State getState() {
        return _state;
    }

    public void setState(State _state) {
        this._state = _state;
    }

    public String getCity() {
        return _city;
    }

    public void setCity(String _city) {
        this._city = _city;
    }
}
