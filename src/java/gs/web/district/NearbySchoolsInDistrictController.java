/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: NearbySchoolsInDistrictController.java,v 1.2 2009/04/01 19:53:04 droy Exp $
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

    public static final String PARAM_DISTRICT_ID = "district_id";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_ACRONYM_OR_NAME = "acronymOrName";
    public static final String MODEL_SCHOOL_LIST = "schoolsWithRatings";
    public static final String MODEL_DISTRICT = "district";
    public static final String MODEL_NUM_ELEMENTARY_SCHOOLS = "numElementarySchools";
    public static final String MODEL_NUM_MIDDLE_SCHOOLS = "numMiddleSchools";
    public static final String MODEL_NUM_HIGH_SCHOOLS = "numHighSchools";
    public static final String MODEL_ACRONYM_OR_NAME = "acronymOrName";
    private IDistrictDao _districtDao;
    private ISchoolDao _schoolDao;
    private IReviewDao _reviewDao;


    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        //SessionContext sc = SessionContextUtil.getSessionContext(request);
        String stateParam = request.getParameter(PARAM_STATE);
        State state = State.fromString(stateParam);

        String districtIdParam = request.getParameter(PARAM_DISTRICT_ID);
        int districtId = Integer.parseInt(districtIdParam);
        District district = _districtDao.findDistrictById(state, districtId);

        String acronymOrName = request.getParameter(PARAM_ACRONYM_OR_NAME);
                
        List<School> schools = _schoolDao.getSchoolsInDistrict(state, districtId, true);
        List<SchoolWithRatings> schoolsWithRatings = _schoolDao.populateSchoolsWithRatings(state, schools);
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

        return modelAndView;
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