/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: CityControllerTest.java,v 1.1 2006/03/15 02:24:21 apeterson Exp $
 */

package gs.web.state;

import gs.web.BaseControllerTestCase;
import gs.web.SessionContextUtil;
import gs.web.MockHttpServletRequest;
import gs.web.util.ListModel;
import gs.web.util.Anchor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.List;

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
        _controller = (CityController) getApplicationContext().
                getBean(CityController.BEAN_ID);
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

        ListModel listModel = (ListModel) model.get(CityController.SCHOOL_BREAKDOWN);

        List list = listModel.getResults();
        assertEquals(4, list.size());

        assertEquals("/search/search.page?c=school&state=AK&city=Anchorage", ((Anchor) list.get(0)).getHref());
        assertEquals("/search/search.page?c=school&state=AK&city=Anchorage&gl=middle", ((Anchor) list.get(2)).getHref());
        assertEquals("All Middle (32)", ((Anchor) list.get(2)).getContents());

    }
}
