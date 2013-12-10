package gs.web.jsp.link;

import gs.data.state.State;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

public class DistrictFeedbackTagHandler extends ContactUsTagHandler {
    private State _state;
    private String _countyName;
    private Integer _districtId;

    public DistrictFeedbackTagHandler() {
        super();
        setFeedbackType("incorrectSchoolDistrictInfo_incorrectDistrict");
    }

    @Override
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.ADD_EDIT_SCHOOL_OR_DISTRICT);
        builder.addParameter("schoolOrDistrict", "district");
        builder.addParameter("addEdit", "edit");
        if (getState() != null) {
            builder.addParameter("state", getState().getAbbreviationLowerCase());
            if (StringUtils.isNotBlank(getCountyName())) {
                builder.addParameter("county", getCountyName());
                if (getDistrictId() != null) {
                    builder.addParameter("districtId", String.valueOf(getDistrictId()));
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

    public String getCountyName() {
        return _countyName;
    }

    public void setCountyName(String countyName) {
        _countyName = countyName;
    }

    public Integer getDistrictId() {
        return _districtId;
    }

    public void setDistrictId(Integer districtId) {
        _districtId = districtId;
    }
}
