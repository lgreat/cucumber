/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: MaximumMssValidator.java,v 1.2 2006/07/11 21:14:04 dlee Exp $
 */
package gs.web.util.validator;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.state.State;
import gs.web.ISessionContext;
import gs.web.SessionContextUtil;
import gs.web.util.UrlBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;

import javax.servlet.http.HttpServletRequest;

/**
 *  Validation passes if can sign up for more MSS
 *
 * @author David Lee <mailto:dlee@greatschools.net>
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
            ISessionContext session = SessionContextUtil.getSessionContext(request);
            State state = session.getStateOrDefault();

            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.NEWSLETTER_MANAGEMENT, state);
            urlBuilder.addParameter("email", user.getEmail());

            String url = urlBuilder.asSiteRelative(request);
            _log.debug(url);

            errors.rejectValue("email", MaximumMssValidator.ERROR_CODE, new Object[] {url}, MaximumMssValidator.DEFAULT_ERROR_MESSAGE);
        }
    }
}
