/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: MaximumMssValidator.java,v 1.7 2009/12/04 22:27:16 chriskimm Exp $
 */
package gs.web.util.validator;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;

import javax.servlet.http.HttpServletRequest;

/**
 *  Validation passes if can sign up for more MSS
 *
 * @author David Lee <mailto:dlee@greatschools.org>
 */
public class MaximumMssValidator implements IRequestAwareValidator {
    public static final String ERROR_CODE = "max_mss_reached";
    public static final String DEFAULT_ERROR_MESSAGE = "You have reached the maximum number of My School Stats.";

    private static final Log _log = LogFactory.getLog(MaximumMssValidator.class);

    IUserDao _userDao;

    public void validate(HttpServletRequest request, Object object, Errors errors) {
        if (object == null) {
            return;
        }

        User user = (User) object;
        if (user.hasReachedMaximumMssSubscriptions()) {
            SessionContext session = SessionContextUtil.getSessionContext(request);
            State state = session.getStateOrDefault();

            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.NEWSLETTER_MANAGEMENT, state);
            urlBuilder.addParameter("email", user.getEmail());

            String url = urlBuilder.asSiteRelative(request);
            _log.debug(url);

            errors.reject(MaximumMssValidator.ERROR_CODE, new Object[] {url}, MaximumMssValidator.DEFAULT_ERROR_MESSAGE);
        }
    }
}
