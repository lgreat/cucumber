package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 6/12/13
 * Time: 10:18 AM
 * To change this template use File | Settings | File Templates.
 */
public class UspFormTagHandler extends BaseSchoolTagHandler {
    @Override
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.USP_FORM);

        if (getSchool() != null) {
            urlBuilder.addParameter("schoolId", String.valueOf(getSchool().getId()));
            urlBuilder.addParameter("state", getSchool().getDatabaseState().getAbbreviationLowerCase());
        }

        return urlBuilder;
    }
}
