package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Handle Feedback Jsp tag
 *
 * Created by IntelliJ IDEA.
 * User: jnorton
 * Date: Nov 27, 2007
 * Time: 12:29:34 PM
 *
 *
 */
public class FeedbackTagHandler extends LinkTagHandler {
    private String _topicOption;

    public void setTopicOption( String topicOption ){
        _topicOption = topicOption;
    }

    public String getTopicOption(){
        return _topicOption;
    }

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.FEEDBACK, getState(), getTopicOption());
        return builder;
    }
}

