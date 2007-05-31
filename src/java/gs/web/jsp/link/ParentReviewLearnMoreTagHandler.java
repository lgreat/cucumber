package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class ParentReviewLearnMoreTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.PARENT_REVIEW_LEARN_MORE, getState());
    }
}
