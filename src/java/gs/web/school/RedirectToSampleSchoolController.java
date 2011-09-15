/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: RedirectToSampleSchoolController.java,v 1.7 2011/09/15 00:36:22 ssprouse Exp $
 */
package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.UrlBuilder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller
 *
 * @author Wendy Beck
 */
public class RedirectToSampleSchoolController extends AbstractController {

    public static final String BEAN_ID = "/school/sampleSchoolRedirect.page";

    private SessionContextUtil _sessionContextUtil;

    private ISchoolDao _schoolDao;

    public ModelAndView handleRequestInternal(HttpServletRequest request,
                                              HttpServletResponse response)
            throws Exception {

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);

        State state = sessionContext.getState();
        School school = _schoolDao.getSampleSchool(state);

        UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        View redirectView = new RedirectView(urlBuilder.asFullUrl(request));
        return new ModelAndView(redirectView);
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;

}

    public SessionContextUtil getSessionContextUtil() {
        return _sessionContextUtil;
    }

    public void setSessionContextUtil(SessionContextUtil sessionContextUtil) {
        _sessionContextUtil = sessionContextUtil;
    }
    }
