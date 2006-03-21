/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: CityControllerTest.java,v 1.5 2006/03/21 01:18:49 apeterson Exp $
 */

package gs.web.state;

import gs.data.school.ISchoolDao;
import gs.data.school.district.IDistrictDao;
import gs.web.BaseControllerTestCase;
import gs.web.MockHttpServletRequest;
import gs.web.SessionContextUtil;
import gs.web.util.Anchor;
import gs.web.util.ListModel;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class CityControllerTest extends BaseControllerTestCase {

    private CityController _controller;
    private SessionContextUtil _sessionContextUtil;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new CityController();
        _controller.setApplicationContext(getApplicationContext());
        _controller.setSchoolDao((ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID));
        _controller.setDistrictDao((IDistrictDao) getApplicationContext().getBean(IDistrictDao.BEAN_ID));
        _sessionContextUtil = (SessionContextUtil) getApplicationContext().
                getBean(SessionContextUtil.BEAN_ID);
    }

    public void testSchoolBreakdown() throws Exception {
        MockHttpServletRequest request = getRequest();
        request.setParameter("state", "AK");
        request.setParameter("city", "Anchorage");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();

        ListModel listModel = (ListModel) model.get(CityController.MODEL_SCHOOL_BREAKDOWN);

        List list = listModel.getResults();
        //assertEquals(4, list.size());

        //assertEquals("/search/search.page?c=school&state=AK&city=Anchorage", ((Anchor) list.get(0)).getHref());
        //assertEquals("/search/search.page?c=school&state=AK&city=Anchorage&gl=middle", ((Anchor) list.get(2)).getHref());
        //assertEquals("All Middle (32)", ((Anchor) list.get(2)).getContents());

    }

    public void testFindDistricts() throws Exception {
        MockHttpServletRequest request = getRequest();
        request.setParameter("state", "WY");
        request.setParameter("city", "Worland");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();

        ListModel listModel = (ListModel) model.get(CityController.MODEL_DISTRICTS);

        List list = listModel.getResults();
        assertEquals(1, list.size());
        assertEquals("/cgi-bin/wy/district_profile/3/", ((Anchor) list.get(0)).getHref());
    }

    public void testCapitalization() {
        // Alison complained that "city=oakland" fails to capitalize as "Oakland".
        MockHttpServletRequest request = getRequest();
        request.setParameter("state", "CA");
        request.setParameter("city", "alameda");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();

        assertEquals("Alameda", model.get(CityController.MODEL_CITY_NAME));

    }
}
