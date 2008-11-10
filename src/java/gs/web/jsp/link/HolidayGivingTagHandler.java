package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class HolidayGivingTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.HOLIDAY_GIVING);
    }
}
