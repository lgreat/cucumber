/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: NearbyCitiesControllerTest.java,v 1.6 2006/07/13 07:53:59 apeterson Exp $
 */

package gs.web.geo;

import gs.data.geo.ICity;
import gs.data.geo.IGeoDao;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.AnchorListModelFactory;
import gs.web.util.list.AnchorListModel;
import gs.web.util.list.Anchor;
import gs.web.util.context.SessionContextUtil;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

/**
 * Provides tests for NearbyCitiesController.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class NearbyCitiesControllerTest extends BaseControllerTestCase {

    private NearbyCitiesController _controller;
    private SessionContextUtil _sessionContextUtil;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new NearbyCitiesController();
        _controller.setApplicationContext(getApplicationContext());
        _controller.setGeoDao((IGeoDao) getApplicationContext().getBean(IGeoDao.BEAN_ID));
        _controller.setAnchorListModelFactory((AnchorListModelFactory) getApplicationContext().getBean(AnchorListModelFactory.BEAN_ID));
        _sessionContextUtil = (SessionContextUtil) getApplicationContext().
                getBean(SessionContextUtil.BEAN_ID);
    }

    public void testAnchorage() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "AK");
        request.setParameter(NearbyCitiesController.PARAM_CITY, "Anchorage");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();

        final ICity city = (ICity) model.get(NearbyCitiesController.MODEL_CITY);
        assertEquals("Anchorage", city.getName());

        final List cities = (List) model.get(NearbyCitiesController.MODEL_CITIES);
        ICity nearestCity  = (ICity) cities.get(0);
        assertEquals("Elmendorf AFB", nearestCity.getName());

        AnchorListModel anchorListModel = (AnchorListModel) model.get(AnchorListModel.DEFAULT);
        final List results = anchorListModel.getResults();
        Anchor anchor = (Anchor) results.get(0);
        assertEquals("Elmendorf AFB", anchor.getContents());
        anchor = (Anchor) results.get(results.size() - 1);
        assertEquals("Sutton", anchor.getContents());
    }

    public void testHeading() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "AK");
        request.setParameter(NearbyCitiesController.PARAM_CITY, "Anchorage");
        request.setParameter(NearbyCitiesController.PARAM_HEADING, "Hey");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();

        AnchorListModel anchorListModel = (AnchorListModel) model.get(AnchorListModel.DEFAULT);
        assertEquals("Hey", anchorListModel.getHeading());
    }
    public void testCount() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "AK");
        request.setParameter(NearbyCitiesController.PARAM_CITY, "Anchorage");
        request.setParameter(NearbyCitiesController.PARAM_HEADING, "Hey");
        request.setParameter(NearbyCitiesController.PARAM_COUNT, "4");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();

        List cities = (List) model.get(NearbyCitiesController.MODEL_CITIES);
        assertEquals(4, cities.size());

        AnchorListModel anchorListModel = (AnchorListModel) model.get(AnchorListModel.DEFAULT);
        List results = anchorListModel.getResults();
        assertEquals(4, results.size());

        // 3
        request.setParameter(NearbyCitiesController.PARAM_COUNT, "3");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        mav = _controller.handleRequestInternal(request, getResponse());

        model = mav.getModel();

        cities = (List) model.get(NearbyCitiesController.MODEL_CITIES);
        assertEquals(3, cities.size());

        anchorListModel = (AnchorListModel) model.get(AnchorListModel.DEFAULT);
        results = anchorListModel.getResults();
        assertEquals(3, results.size());

        // 2
        request.setParameter(NearbyCitiesController.PARAM_COUNT, "2");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        mav = _controller.handleRequestInternal(request, getResponse());

        model = mav.getModel();

        cities = (List) model.get(NearbyCitiesController.MODEL_CITIES);
        assertEquals(2, cities.size());

        anchorListModel = (AnchorListModel) model.get(AnchorListModel.DEFAULT);
        results = anchorListModel.getResults();
        assertEquals(2, results.size());

        // 1
        request.setParameter(NearbyCitiesController.PARAM_COUNT, "1");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        mav = _controller.handleRequestInternal(request, getResponse());

        model = mav.getModel();

        cities = (List) model.get(NearbyCitiesController.MODEL_CITIES);
        assertEquals(1, cities.size());

        anchorListModel = (AnchorListModel) model.get(AnchorListModel.DEFAULT);
        results = anchorListModel.getResults();
        assertEquals(1, results.size());
    }


    public void testMore() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "AK");
        request.setParameter(NearbyCitiesController.PARAM_CITY, "Anchorage");
        request.setParameter(NearbyCitiesController.PARAM_HEADING, "Hey");
        request.setParameter(NearbyCitiesController.PARAM_COUNT, "4");
        request.setParameter(NearbyCitiesController.PARAM_MORE, "1");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();

        List cities = (List) model.get(NearbyCitiesController.MODEL_CITIES);
        assertEquals(4, cities.size());

        AnchorListModel anchorListModel = (AnchorListModel) model.get(AnchorListModel.DEFAULT);
        List results = anchorListModel.getResults();
        assertEquals(5, results.size());

        Anchor anchor = (Anchor) results.get(results.size() - 1);
        assertEquals("More", anchor.getContents());
        assertEquals("/cities.page?all=1&city=Anchorage&includeState=1&order=alpha&state=AK", anchor.getHref());
    }

    public void testAll() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "AK");
        request.setParameter(NearbyCitiesController.PARAM_CITY, "Anchorage");
        request.setParameter(NearbyCitiesController.PARAM_HEADING, "Hey");
        request.setParameter(NearbyCitiesController.PARAM_COUNT, "4");
        request.setParameter(NearbyCitiesController.PARAM_ALL, "1");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();

        List cities = (List) model.get(NearbyCitiesController.MODEL_CITIES);
        assertEquals(4, cities.size());

        AnchorListModel anchorListModel = (AnchorListModel) model.get(AnchorListModel.DEFAULT);
        List results = anchorListModel.getResults();
        assertEquals(5, results.size());

        Anchor anchor = (Anchor) results.get(results.size() - 1);
        assertEquals("Browse all Alaska cities", anchor.getContents());
        assertEquals("/modperl/cities/AK/", anchor.getHref());

    }
    public void testMoreAndAll() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "AK");
        request.setParameter(NearbyCitiesController.PARAM_CITY, "Anchorage");
        request.setParameter(NearbyCitiesController.PARAM_HEADING, "Hey");
        request.setParameter(NearbyCitiesController.PARAM_COUNT, "4");
        request.setParameter(NearbyCitiesController.PARAM_MORE, "1");
        request.setParameter(NearbyCitiesController.PARAM_ALL, "1");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();

        List cities = (List) model.get(NearbyCitiesController.MODEL_CITIES);
        assertEquals(4, cities.size());

        AnchorListModel anchorListModel = (AnchorListModel) model.get(AnchorListModel.DEFAULT);
        List results = anchorListModel.getResults();
        assertEquals(6, results.size());

        Anchor anchor = (Anchor) results.get(results.size() - 2);
        assertEquals("More", anchor.getContents());
        assertEquals("/cities.page?all=1&city=Anchorage&includeState=1&order=alpha&state=AK", anchor.getHref());

         anchor = (Anchor) results.get(results.size() - 1);
        assertEquals("Browse all Alaska cities", anchor.getContents());
        assertEquals("/modperl/cities/AK/", anchor.getHref());

    }
}
