package gs.web.jsp.link.school;

import com.google.gdata.util.common.net.UriEncoder;
import gs.web.util.UrlBuilder;

/**
 * @author aroy@greatschools.org
 */
public class EspRegisterTagHandler extends BaseSchoolTagHandler {

    @Override
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_REGISTRATION);

        if (getSchool() != null) {
            urlBuilder.addParameter("schoolId", String.valueOf(getSchool().getId()));
            //encode possible spaces and possible special characters in city name
            urlBuilder.addParameter("city", UriEncoder.encode(String.valueOf(getSchool().getCity())));
            urlBuilder.addParameter("state", getSchool().getDatabaseState().getAbbreviationLowerCase());
        }

        return urlBuilder;
    }
}
