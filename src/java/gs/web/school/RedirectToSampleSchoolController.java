/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: RedirectToSampleSchoolController.java,v 1.1 2006/03/14 18:39:34 wbeck Exp $
 */
package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.SessionContextUtil;
import gs.web.SessionFacade;
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

       //ISessionFacade sessionContext = SessionContext.getInstance(request);
        ISessionFacade sessionContext = SessionFacade.getInstance(request);

        State state = sessionContext.getState();
        School school = _schoolDao.getSampleSchool(state);

        View redirectView = new RedirectView("/modperl/browse_school/"+state.getAbbreviationLowerCase()+"/"+school.getId()+"/");
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
