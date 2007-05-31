package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class ParentRatingsExplainedTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.PARENT_RATING_EXPLAINED, getState());
    }
}
