package gs.web.jsp.link;

import gs.data.state.State;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolFeedbackTagHandler extends ContactUsTagHandler {
    private State _state;

    public SchoolFeedbackTagHandler() {
        super();
        setFeedbackType("incorrectSchoolDistrictInfo_incorrectSchool");
    }

    @Override
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.ADD_EDIT_SCHOOL_OR_DISTRICT);
        builder.addParameter("schoolOrDistrict", "school");
        builder.addParameter("addEdit", "edit");
        if (getState() != null) {
            builder.addParameter("state", getState().getAbbreviationLowerCase());
            if (StringUtils.isNotBlank(getCityName())) {
                builder.addParameter("cityName", getCityName());
                if (getSchoolId() != null) {
                    builder.addParameter("schoolId", String.valueOf(getSchoolId()));
                }
            }
        }
        return builder;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }
}
