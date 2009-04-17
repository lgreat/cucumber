/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: NearbySchoolsInDistrictController.java,v 1.6 2009/04/17 22:58:38 droy Exp $
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
 * Generates a model to show recent parent reviews in a geographical region.
 * Inputs:
 * <li>state - required
 * <li>city - optional
 * <li>max - optional limit on the number of reviews to show. Default is 3.
 * Output model:
 * <li>reviews - a List of IParentReviewModel objects
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class NearbySchoolsInDistrictController extends AbstractController {
    private static final Logger _log = Logger.getLogger(NearbySchoolsInDistrictController.class);

    public static final int MAX_SCHOOLS_IN_MAP = 100;

    public static final String PARAM_DISTRICT_ID = "district_id";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_ACRONYM_OR_NAME = "acronymOrName";
    public static final String PARAM_SHOW_E = "show_e";
    public static final String PARAM_SHOW_M = "show_m";
    public static final String PARAM_SHOW_H = "show_h";
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
                
        List<SchoolWithRatings> schoolsWithRatings = _schoolDao.findTopRatedSchoolsInDistrict(district, 0, LevelCode.ELEMENTARY_MIDDLE_HIGH, MAX_SCHOOLS_IN_MAP);
        _reviewDao.loadRatingsIntoSchoolList(schoolsWithRatings, state);

        int num_elementary_schools = _schoolDao.countSchoolsInDistrict(state, null, LevelCode.ELEMENTARY, districtIdParam);
        int num_middle_schools = _schoolDao.countSchoolsInDistrict(state, null, LevelCode.MIDDLE, districtIdParam);
        int num_high_schools = _schoolDao.countSchoolsInDistrict(state, null, LevelCode.HIGH, districtIdParam);

        ModelAndView modelAndView = new ModelAndView("/district/nearbySchoolsInDistrict");
        modelAndView.addObject(MODEL_SCHOOL_LIST, schoolsWithRatings);
        modelAndView.addObject(MODEL_DISTRICT, district);
        modelAndView.addObject(MODEL_NUM_ELEMENTARY_SCHOOLS, num_elementary_schools);
        modelAndView.addObject(MODEL_NUM_MIDDLE_SCHOOLS, num_middle_schools);
        modelAndView.addObject(MODEL_NUM_HIGH_SCHOOLS, num_high_schools);
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