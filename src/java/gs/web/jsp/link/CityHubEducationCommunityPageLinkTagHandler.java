package gs.web.jsp.link;

import gs.data.state.State;
import gs.web.util.UrlBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: sarora
 * Date: 10/22/13
 * Time: 1:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class CityHubEducationCommunityPageLinkTagHandler  extends LinkTagHandler {
    private State _state;
    private String _city;

    @Override
    public UrlBuilder createUrlBuilder() {
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.EDUCATION_COMMUNITY_SCHOOLS_PAGE, getState(), getCity());
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
