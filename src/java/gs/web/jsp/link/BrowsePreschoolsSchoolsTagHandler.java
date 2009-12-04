package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.state.State;

/**
 * Created by chriskimm@greatschools.org
 *
 * This link tag handler creates a link to the browse schools page with preschools selected.
 */
public class BrowsePreschoolsSchoolsTagHandler extends LinkTagHandler {

    /** required State param */
    private State _state;
    
    /** required city-as-String param */
    private String _city;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.BROWSE_PRESCHOOLS, getState(), getCity());
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public String getCity() {
        return _city;
    }

    public void setCity(String city) {
        _city = city;
    }
}
