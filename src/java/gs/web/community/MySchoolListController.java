/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: MySchoolListController.java,v 1.1 2005/11/23 00:14:58 apeterson Exp $
 */

package gs.web.community;

import gs.data.community.FavoriteSchool;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.SessionContextUtil;
import gs.web.SessionFacade;
import gs.web.util.Anchor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class MySchoolListController extends AbstractController {
    private String _viewName;
    private ISchoolDao _schoolDao;
    private IUserDao _userDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ISessionFacade context = SessionFacade.getInstance(request);
        String memberId = request.getParameter(SessionContextUtil.MEMBER_PARAM);

        State state = context.getStateOrDefault();

        User user = _userDao.getUserFromId(Integer.valueOf(memberId).intValue());

        Map model = new HashMap();

        model.put("header", user.getFirstName() + ": My School List");

        Set schools = user.getFavoriteSchools();

        List items = new ArrayList(schools.size());
        for (Iterator i = schools.iterator(); i.hasNext();) {
            FavoriteSchool favoriteSchool = (FavoriteSchool) i.next();

            School school = _schoolDao.getSchoolById(favoriteSchool.getState(), favoriteSchool.getId());

            Anchor anchor = new Anchor("/modperl/browse_school/"+ school.getDatabaseState().getAbbreviationLowerCase()+
                    "/"+school.getId()+"/",
                    school.getName());
            items.add(anchor);
        }
        items.add(new Anchor("/cgi-bin/msl_confirm/" + state.getAbbreviation() + "/",
                "Manage My School List",
                "viewall"));
        model.put("results", items);


        ModelAndView modelAndView = new ModelAndView(_viewName, model);
        return modelAndView;
    }


    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
