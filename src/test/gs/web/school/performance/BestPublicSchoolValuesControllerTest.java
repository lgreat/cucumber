/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: BestPublicSchoolValuesControllerTest.java,v 1.7 2006/07/13 07:53:59 apeterson Exp $
 */

package gs.web.school.performance;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContextUtil;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

/**
 * Tests BestPublicSchoolValuesController.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class BestPublicSchoolValuesControllerTest extends BaseControllerTestCase {

    private BestPublicSchoolValuesController _controller;
    private SessionContextUtil _sessionContextUtil;

    protected void setUp() throws Exception {
        super.setUp();

        _controller = new BestPublicSchoolValuesController();
        _controller.setViewName("/school/performance/schoolValues");
        _controller.setApplicationContext(getApplicationContext());

        _sessionContextUtil = (SessionContextUtil) getApplicationContext().getBean(SessionContextUtil.BEAN_ID);
    }

    public void testBpsv() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        _controller.setShowingRank(true);
        _controller.setShowingAll(false);
        _controller.setTitle("All Bay Area Cities");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        List cityList = (List) model.get(BestPublicSchoolValuesController.MODEL_CITY_LIST);
        String subtitle = (String) model.get(BestPublicSchoolValuesController.MODEL_PAGE_TITLE);
        Boolean showRank = (Boolean) model.get(BestPublicSchoolValuesController.MODEL_SHOW_RANK);

        assertEquals("Albany", ((BestPublicSchoolValuesController.IBestPublicSchoolValue) cityList.get(0)).getCityName());
        assertEquals("All Bay Area Cities", subtitle);
        assertTrue(showRank.booleanValue());
    }

}
