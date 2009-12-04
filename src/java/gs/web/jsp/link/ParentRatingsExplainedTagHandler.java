package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * @author <a href="mailto:dlee@greatschools.org">David Lee</a>
 */
public class ParentRatingsExplainedTagHandler extends LinkTagHandler {
    private boolean _preschool = false;
    protected UrlBuilder createUrlBuilder() {
        if (isPreschool()) {
            return new UrlBuilder(UrlBuilder.PARENT_RATING_PRESCHOOL_EXPLAINED, getState());
        } else {
            return new UrlBuilder(UrlBuilder.PARENT_RATING_EXPLAINED, getState());
        }
    }

    public boolean isPreschool() {
        return _preschool;
    }

    public void setPreschool(boolean preschool) {
        _preschool = preschool;
    }
}
