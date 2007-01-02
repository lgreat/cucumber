/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: RedirectToSampleSchoolController.java,v 1.4 2007/01/02 20:09:16 cpickslay Exp $
 */
package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;
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

       //SessionContext sessionContext = SessionContext.getInstance(request);
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);

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
