package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * Created by IntelliJ IDEA.
 * User: eddie
 * Date: Mar 24, 2010
 * Time: 6:32:43 PM
 * To change this template use File | Settings | File Templates.
 */
/**
 * Generate a tag to the page where the user manages their newsletters.
 */
public class NewsletterUnsubscribeTagHandler extends LinkTagHandler {
    private String _email;
    private String _ref;

    protected UrlBuilder createUrlBuilder() {

        UrlBuilder builder = new UrlBuilder(UrlBuilder.NEWSLETTER_UNSUBSCRIBE);

        if (StringUtils.isNotBlank(_email)) {
            builder.setParameter("email", _email);
        }
        if (StringUtils.isNotBlank(_ref)) {
            builder.setParameter("ref", _ref);
        }


        return builder;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public void setRef (String ref) {
        _ref = ref;
    }

}
