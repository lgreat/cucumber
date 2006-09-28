/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: RatingsController.java,v 1.2 2006/09/28 01:04:16 dlee Exp $
 */
package gs.web.test.rating;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.ITestDataSetDao;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Draw the rating page
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class RatingsController extends AbstractController {

    private static final Log _log = LogFactory.getLog(RatingsController.class);

    private String _viewName;
    private ISchoolDao _schoolDao;
    private IRatingsConfigDao _ratingsConfigDao;
    private ITestDataSetDao _testDataSetDao;

    /**
     * @todo deal with error of no id
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map model = new HashMap();

        Integer schoolId = new Integer(1);
        String schoolIdStr = request.getParameter("id");
        if (StringUtils.isNumeric(schoolIdStr)) {
            schoolId = new Integer(schoolIdStr);
        }

        String detailStr = request.getParameter("details");

        State state = SessionContextUtil.getSessionContext(request).getState();

        School school = _schoolDao.getSchoolById(state, schoolId);

        IRatingsConfig ratingsConfig = _ratingsConfigDao.restoreRatingsConfig(state);

        SchoolRatingsDisplay ratingsDisplay = new SchoolRatingsDisplay(ratingsConfig, school, _testDataSetDao);
        OverallRatingDecorator ratingDecorator = new OverallRatingDecorator(ratingsDisplay);

        model.put("columns", ratingDecorator.getSubjectGroupLabels());
        model.put("rowGroups", ratingDecorator.getRowGroups());
        model.put("school", school);

        return new ModelAndView(_viewName, model);
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

    public void setRatingsConfigDao(final IRatingsConfigDao ratingsConfigDao) {
        _ratingsConfigDao = ratingsConfigDao;
    }

    public void setTestDataSetDao(final ITestDataSetDao testDataSetDao) {
        _testDataSetDao = testDataSetDao;
    }
}