/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: MySchoolListController.java,v 1.11 2006/04/28 06:53:09 apeterson Exp $
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
import gs.web.util.ListModel;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Provide the "member" and "limit". Returns data in the ListModel format.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 * @see #PARAM_LIMIT
 * @see SessionContextUtil.MEMBER_PARAM
 * @see ListModel
 */
public class MySchoolListController extends AbstractController {
    private String _viewName;
    private ISchoolDao _schoolDao;
    private IUserDao _userDao;
    public static final int DEFAULT_SCHOOL_LIMIT = 3;

    /**
     * The maximum number of schools to display.
     */
    public static final String PARAM_LIMIT = "limit";


    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ISessionFacade context = SessionFacade.getInstance(request);
        String memberId = request.getParameter(SessionContextUtil.MEMBER_PARAM);
        String limitStr = request.getParameter(PARAM_LIMIT);
        int limit = StringUtils.isNumeric(limitStr) ? Integer.valueOf(limitStr).intValue() : DEFAULT_SCHOOL_LIMIT;


        State state = context.getStateOrDefault();

        User user = _userDao.getUserFromId(Integer.valueOf(memberId).intValue());

        Map model = new HashMap();

        Set schools = user.getFavoriteSchools();

        List items = new ArrayList(schools.size());
        int shown = 0;
        for (Iterator i = schools.iterator(); i.hasNext();) {
            FavoriteSchool favoriteSchool = (FavoriteSchool) i.next();

            School school = _schoolDao.getSchoolById(favoriteSchool.getState(), favoriteSchool.getSchoolId());

            if (shown < limit) {
                UrlBuilder builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
                items.add(builder.asAnchor(request, school.getName()));
                shown++;
            } else if (shown == limit) {
                UrlBuilder builder = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST, state);
                items.add(builder.asAnchor(request,
                        "" + (schools.size() - limit) + " more... (view)",
                        "viewall"));
                shown = schools.size();
            }
        }
        model.put(ListModel.RESULTS, items);


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
