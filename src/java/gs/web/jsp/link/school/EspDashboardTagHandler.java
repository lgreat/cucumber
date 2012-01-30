package gs.web.jsp.link.school;

import gs.data.state.State;
import gs.web.jsp.link.LinkTagHandler;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * @author aroy@greatschools.org
 */
public class EspDashboardTagHandler extends LinkTagHandler {
    private State _defaultState;
    private String _defaultCity;
    private Integer _defaultSchoolId;

    @Override
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
        
        if (_defaultState != null) {
            urlBuilder.addParameter("defaultState", _defaultState.getAbbreviationLowerCase());
            if (StringUtils.isNotEmpty(_defaultCity)) {
                urlBuilder.addParameter("defaultCity", _defaultCity);
                if (_defaultSchoolId != null) {
                    urlBuilder.addParameter("defaultSchoolId", String.valueOf(_defaultSchoolId));
                }
            }
        }
        
        return urlBuilder;
    }

    public State getDefaultState() {
        return _defaultState;
    }

    public void setDefaultState(State defaultState) {
        _defaultState = defaultState;
    }

    public String getDefaultCity() {
        return _defaultCity;
    }

    public void setDefaultCity(String defaultCity) {
        _defaultCity = defaultCity;
    }

    public Integer getDefaultSchoolId() {
        return _defaultSchoolId;
    }

    public void setDefaultSchoolId(Integer defaultSchoolId) {
        _defaultSchoolId = defaultSchoolId;
    }
}
