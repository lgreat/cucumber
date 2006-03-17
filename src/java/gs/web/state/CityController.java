/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: CityController.java,v 1.3 2006/03/17 22:33:24 apeterson Exp $
 */

package gs.web.state;

import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.web.SessionContext;
import gs.web.util.Anchor;
import gs.web.util.ListModel;
import gs.web.util.UrlUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class CityController extends AbstractController {

    public static final String BEAN_ID = "/city.page";

    private static final String PARAM_CITY = "city";

    public static final String MODEL_DISTRICTS = "districts"; // ListModel object
    public static final String MODEL_SCHOOL_BREAKDOWN = "schoolBreakdown"; // ListModel object


    private ISchoolDao _schoolDao;
    private IDistrictDao _districtDao;
    private final UrlUtil _urlUtil;

    public CityController() {
        _urlUtil = new UrlUtil();
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        State state = SessionContext.getInstance(request).getStateOrDefault();
        String cityNameParam = request.getParameter(PARAM_CITY);


        Map model = new HashMap();

        ListModel schoolBreakdownList = createSchoolSummaryModel(state, cityNameParam);
        model.put(MODEL_SCHOOL_BREAKDOWN, schoolBreakdownList);

        ListModel districtList = createDistrictList(state, cityNameParam, request);
        model.put(MODEL_DISTRICTS, districtList);

        return new ModelAndView("test/city2", model);
    }

    private ListModel createDistrictList(State state, String cityNameParam, HttpServletRequest request) {
        ListModel districts = new ListModel();

        List list = _districtDao.findDistrictsInCity(state, cityNameParam, true);
        if (list != null) {
            districts.setHeading(cityNameParam + " Districts");
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                District d = (District) iter.next();
                String url = "/cgi-bin/" + state.getAbbreviationLowerCase() + "/district_profile/" + d.getId() + "/";
                url = _urlUtil.buildUrl(url, request);
                districts.addResult(new Anchor(url, d.getName()));
            }
        }

        return districts;
    }

    private ListModel createSchoolSummaryModel(State state, String cityNameParam) {
        // the summaries of schools in a city
        ListModel schoolBreakdownList;
        schoolBreakdownList = new ListModel();

        int sc = _schoolDao.countSchools(state, null, null, cityNameParam);
        if (sc > 0) {
            Anchor a = new Anchor("/search/search.page?c=school&state=" + state.getAbbreviation() + "&city=" + cityNameParam,
                    "All " + cityNameParam + " schools (" + sc + ")");
            schoolBreakdownList.addResult(a);

            sc = _schoolDao.countSchools(state, null, LevelCode.ELEMENTARY, cityNameParam);
            if (sc > 0) {
                a = new Anchor("/search/search.page?c=school&state=" + state.getAbbreviation() + "&city=" + cityNameParam + "&gl=elementary",
                        "All Elementary (" + sc + ")");
                schoolBreakdownList.addResult(a);
            }

            sc = _schoolDao.countSchools(state, null, LevelCode.MIDDLE, cityNameParam);
            if (sc > 0) {
                a = new Anchor("/search/search.page?c=school&state=" + state.getAbbreviation() + "&city=" + cityNameParam + "&gl=middle",
                        "All Middle (" + sc + ")");
                schoolBreakdownList.addResult(a);
            }

            sc = _schoolDao.countSchools(state, null, LevelCode.HIGH, cityNameParam);
            if (sc > 0) {
                a = new Anchor("/search/search.page?c=school&state=" + state.getAbbreviation() + "&city=" + cityNameParam + "&gl=high",
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

    public IDistrictDao getDistrictDao() {
        return _districtDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
    }
}
