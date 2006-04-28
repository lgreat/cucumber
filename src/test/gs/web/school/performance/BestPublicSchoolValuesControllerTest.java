/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: BestPublicSchoolValuesControllerTest.java,v 1.1 2006/04/28 06:00:35 apeterson Exp $
 */

package gs.web.school.performance;

import gs.web.BaseControllerTestCase;
import gs.web.SessionContextUtil;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.ListModel;
import gs.web.school.SchoolsController;
import gs.data.school.district.IDistrictDao;
import gs.data.school.School;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.search.Searcher;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.List;

/**
 * Tests SchoolsController.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class BestPublicSchoolValuesControllerTest extends BaseControllerTestCase {

    private BestPublicSchoolValuesController _controller;
    private SessionContextUtil _sessionContextUtil;

    protected void setUp() throws Exception {
        super.setUp();

        _controller = new BestPublicSchoolValuesController();
        _controller.setApplicationContext(getApplicationContext());

        _sessionContextUtil = (SessionContextUtil) getApplicationContext().getBean(SessionContextUtil.BEAN_ID);
    }

    public void testBpsv() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        request.setParameter(BestPublicSchoolValuesController.PARAM_LIST,
                BestPublicSchoolValuesController.PARAM_LIST_VALUE_ABOVE_AVG_API);
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        List cityList = (List) model.get(BestPublicSchoolValuesController.MODEL_CITY_LIST);
        ListModel links= (ListModel) model.get(BestPublicSchoolValuesController.MODEL_LINKS);
        String subtitle = (String) model.get(BestPublicSchoolValuesController.MODEL_PAGE_SUBTITLE);
        Boolean showMap = (Boolean) model.get(BestPublicSchoolValuesController.MODEL_SHOW_MAP);
        Boolean showRank = (Boolean) model.get(BestPublicSchoolValuesController.MODEL_SHOW_RANK);

        assertEquals("Albany", ((BestPublicSchoolValuesController.IBestPublicSchoolValue)cityList.get(0)).getCityName());
        assertEquals("All Bay Area Cities", subtitle);
        assertFalse(showMap.booleanValue());
        assertTrue(showRank.booleanValue());
    }

}
