/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: NearbySchoolsInDistrictController.java,v 1.15 2012/08/30 16:36:18 npatury Exp $
 */

package gs.web.district;

import gs.data.school.*;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.IReviewDao;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Renders a map with schools in the  district.
 *
 * district_id - required - District to show schools in
 * state - required - State the district id is in
 * acronymOrName - required - Either the name of the district or the acronym for it (the acronym isn't stored in
 *                            the db, so it must be passed)
 * show_e, show_m, show_h - optional - If one or more of these arguments are passed and are set to "1" those level
 *                                     filter checkboxes will be selected.  If none are passed, then all level filters
 *                                     will be selected by default.
 *
 * @author <a href="mailto:droy@greatschools.org">Dave Roy</a>
 */
public class NearbySchoolsInDistrictController extends AbstractController {
    private static final Logger _log = Logger.getLogger(NearbySchoolsInDistrictController.class);

    public static final int MAX_SCHOOLS_IN_MAP = 125;

    public static final String PARAM_DISTRICT_ID = "district_id";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_ACRONYM_OR_NAME = "acronymOrName";
    public static final String PARAM_SHOW_E = "show_e";
    public static final String PARAM_SHOW_M = "show_m";
    public static final String PARAM_SHOW_H = "show_h";
    public static final String PARAM_NUM_ELEMENTARY_SCHOOLS = "numElementarySchools";
    public static final String PARAM_NUM_MIDDLE_SCHOOLS = "numMiddleSchools";
    public static final String PARAM_NUM_HIGH_SCHOOLS = "numHighSchools";
    public static final String MODEL_SCHOOL_LIST = "schoolsWithRatings";
    public static final String MODEL_DISTRICT = "district";
    public static final String MODEL_NUM_ELEMENTARY_SCHOOLS = "numElementarySchools";
    public static final String MODEL_NUM_MIDDLE_SCHOOLS = "numMiddleSchools";
    public static final String MODEL_NUM_HIGH_SCHOOLS = "numHighSchools";
    public static final String MODEL_ACRONYM_OR_NAME = "acronymOrName";
    public static final String MODEL_SHOW_E = "show_e";
    public static final String MODEL_SHOW_M = "show_m";
    public static final String MODEL_SHOW_H = "show_h";
    private IDistrictDao _districtDao;
    private ISchoolDao _schoolDao;
    private IReviewDao _reviewDao;


    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String stateParam = request.getParameter(PARAM_STATE);
        State state = State.fromString(stateParam);

        String districtIdParam = request.getParameter(PARAM_DISTRICT_ID);
        int districtId = Integer.parseInt(districtIdParam);
        District district = _districtDao.findDistrictById(state, districtId);

        String acronymOrName = request.getParameter(PARAM_ACRONYM_OR_NAME);

        List<SchoolWithRatings> schoolsWithRatings = _schoolDao.findTopRatedSchoolsInDistrictNewGSRating(district, 0, LevelCode.ELEMENTARY_MIDDLE_HIGH, MAX_SCHOOLS_IN_MAP);
        _reviewDao.loadRatingsIntoSchoolList(schoolsWithRatings, state);
        ModelAndView modelAndView = new ModelAndView("/district/nearbySchoolsInDistrict");
        modelAndView.addObject(MODEL_SCHOOL_LIST, schoolsWithRatings);
        modelAndView.addObject(MODEL_DISTRICT, district);
        modelAndView.addObject(MODEL_NUM_ELEMENTARY_SCHOOLS, request.getParameter(PARAM_NUM_ELEMENTARY_SCHOOLS));
        modelAndView.addObject(MODEL_NUM_MIDDLE_SCHOOLS, request.getParameter(PARAM_NUM_MIDDLE_SCHOOLS));
        modelAndView.addObject(MODEL_NUM_HIGH_SCHOOLS, request.getParameter(PARAM_NUM_HIGH_SCHOOLS));
        modelAndView.addObject(MODEL_ACRONYM_OR_NAME, acronymOrName);

        setDefaultLevelCodeFilters(request, modelAndView);

        return modelAndView;
    }

    protected void setDefaultLevelCodeFilters(HttpServletRequest request, ModelAndView modelAndView) {
        String show_e = request.getParameter(PARAM_SHOW_E);
        String show_m = request.getParameter(PARAM_SHOW_M);
        String show_h = request.getParameter(PARAM_SHOW_H);

        if (StringUtils.isBlank(show_e) && StringUtils.isBlank(show_m) && StringUtils.isBlank(show_h)) {
            // If no levels are explicitly specified default to all showing
            modelAndView.addObject(MODEL_SHOW_E, "true");
            modelAndView.addObject(MODEL_SHOW_M, "true");
            modelAndView.addObject(MODEL_SHOW_H, "true");
        } else {
            if (show_e.equals("1")) {
                modelAndView.addObject(MODEL_SHOW_E, "true");
            } else {
                modelAndView.addObject(MODEL_SHOW_E, "");
            }
            if (request.getParameter(PARAM_SHOW_M).equals("1")) {
                modelAndView.addObject(MODEL_SHOW_M, "true");
            } else {
                modelAndView.addObject(MODEL_SHOW_M, "");
            }
            if (request.getParameter(PARAM_SHOW_H).equals("1")) {
                modelAndView.addObject(MODEL_SHOW_H, "true");
            } else {
                modelAndView.addObject(MODEL_SHOW_H, "");
            }
        }
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
    }
}