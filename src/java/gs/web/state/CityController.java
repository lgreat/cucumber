/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: CityController.java,v 1.2 2006/03/17 05:44:19 apeterson Exp $
 */

package gs.web.state;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

import gs.web.util.ListModel;
import gs.web.util.Anchor;
import gs.web.SessionContext;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.state.State;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class CityController extends AbstractController {

    public static final String BEAN_ID = "/city.page";

    public static final String SCHOOL_BREAKDOWN = "schoolBreakdown";


    private static final String PARAM_CITY = "city";

    ISchoolDao _schoolDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        State state = SessionContext.getInstance(request).getStateOrDefault();
        String cityNameParam = request.getParameter(PARAM_CITY);


        Map model = new HashMap();

        ListModel schoolBreakdownList = createSchoolSummaryModel(state, cityNameParam);

        model.put(SCHOOL_BREAKDOWN, schoolBreakdownList);

        return new ModelAndView("test/city2", model);
    }

    private ListModel createSchoolSummaryModel(State state, String cityNameParam) {
        // the summaries of schools in a city
        ListModel schoolBreakdownList;
        schoolBreakdownList = new ListModel();

        int sc = _schoolDao.countSchools(state, null, null, cityNameParam);
        if (sc > 0) {
            Anchor a = new Anchor("/search/search.page?c=school&state="+state.getAbbreviation()+"&city=" + cityNameParam,
                    "All " + cityNameParam + " schools ("+sc+")");
            schoolBreakdownList.addResult(a);

            sc = _schoolDao.countSchools(state, null, LevelCode.ELEMENTARY, cityNameParam);
            if (sc > 0) {
                a = new Anchor("/search/search.page?c=school&state=" + state.getAbbreviation() + "&city=" + cityNameParam+"&gl=elementary",
                        "All Elementary (" + sc + ")");
                schoolBreakdownList.addResult(a);
            }

            sc = _schoolDao.countSchools(state, null, LevelCode.MIDDLE, cityNameParam);
            if (sc > 0) {
                a = new Anchor("/search/search.page?c=school&state=" + state.getAbbreviation() + "&city=" + cityNameParam+"&gl=middle",
                        "All Middle (" + sc + ")");
                schoolBreakdownList.addResult(a);
            }

            sc = _schoolDao.countSchools(state, null, LevelCode.HIGH, cityNameParam);
            if (sc > 0) {
                a = new Anchor("/search/search.page?c=school&state=" + state.getAbbreviation() + "&city=" + cityNameParam+"&gl=high",
                        "All High (" + sc + ")");
                schoolBreakdownList.addResult(a);
            }

        }
        return schoolBreakdownList;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }
}
