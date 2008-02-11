package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

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
    private String _redirect;

    public void setTopicOption( String topicOption ){
        _topicOption = topicOption;
    }

    public String getTopicOption(){
        return _topicOption;
    }

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.FEEDBACK, getState(), getTopicOption());
        if (!StringUtils.isBlank(_redirect)) {
            builder.addParameter("redirect", _redirect);
        }
        return builder;
    }

    public String getRedirect() {
        return _redirect;
    }

    public void setRedirect(String redirect) {
        _redirect = redirect;
    }
}

