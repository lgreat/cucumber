/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: RecentParentReviewsControllerTest.java,v 1.11 2009/12/04 20:54:19 npatury Exp $
 */

package gs.web.school.review;

import gs.data.school.review.IReviewDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContextUtil;
import static org.easymock.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class RecentParentReviewsControllerTest extends BaseControllerTestCase {

    private RecentParentReviewsController _controller;
    private SessionContextUtil _sessionContextUtil;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new RecentParentReviewsController();
        _controller.setApplicationContext(getApplicationContext());
        _controller.setReviewDao((IReviewDao) getApplicationContext().getBean(IReviewDao.BEAN_ID));
        _sessionContextUtil = (SessionContextUtil) getApplicationContext().
                getBean(SessionContextUtil.BEAN_ID);
        RecentParentReviewsController.DEFAULT_MAX_AGE = 99999;
    }

    public void testWithCity() throws Exception {
        IReviewDao reviewDao = createMock(IReviewDao.class);
        expect(reviewDao.findRecentReviewsInCity(State.AK,"Anchorage", 10, RecentParentReviewsController.DEFAULT_MAX_AGE)).
                andReturn(new ArrayList<Integer>());
        replay(reviewDao);
        _controller.setReviewDao(reviewDao);

        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "AK");
        request.setParameter("city", "Anchorage");
        request.setParameter(RecentParentReviewsController.PARAM_MAX, "10");
        _sessionContextUtil.prepareSessionContext(request, getResponse());        

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        List<IParentReviewModel> parentReviewList =
                (List) mav.getModel().get(RecentParentReviewsController.MODEL_REVIEW_LIST);

        assertNotNull(parentReviewList);
    }

    public void testWithDistrict() throws Exception {
        IReviewDao reviewDao = createMock(IReviewDao.class);
        expect(reviewDao.findRecentReviewsInDistrict(State.CA,1, 3, RecentParentReviewsController.DEFAULT_MAX_AGE)).
                andReturn(new ArrayList<Integer>());
        replay(reviewDao);
        _controller.setReviewDao(reviewDao);

        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "CA");
        request.setParameter(RecentParentReviewsController.PARAM_DISTRICT_ID, "1");
        request.setParameter(RecentParentReviewsController.PARAM_MAX, "3");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        List<IParentReviewModel> parentReviewList =
                (List) mav.getModel().get(RecentParentReviewsController.MODEL_REVIEW_LIST);

        assertNotNull(parentReviewList);
    }

    public void testWithBlankDistrict() throws Exception {
        IReviewDao reviewDao = createMock(IReviewDao.class);
        // Expect no DAO methods to be called if the district id is blank
        replay(reviewDao);
        _controller.setReviewDao(reviewDao);

        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "CA");
        request.setParameter(RecentParentReviewsController.PARAM_DISTRICT_ID, "");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        List<IParentReviewModel> parentReviewList =
                (List) mav.getModel().get(RecentParentReviewsController.MODEL_REVIEW_LIST);

        assertNotNull(parentReviewList);
    }

    public void testWithNullDistrict() throws Exception {
        IReviewDao reviewDao = createMock(IReviewDao.class);
        // Expect no DAO methods to be called if the district id is blank
        replay(reviewDao);
        _controller.setReviewDao(reviewDao);

        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "CA");
        request.setParameter(RecentParentReviewsController.PARAM_DISTRICT_ID, (String)null);
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        List<IParentReviewModel> parentReviewList =
                (List) mav.getModel().get(RecentParentReviewsController.MODEL_REVIEW_LIST);

        assertNotNull(parentReviewList);
    }

    public void testWithNothing() throws Exception {
        IReviewDao reviewDao = createMock(IReviewDao.class);
        // Expect no DAO methods to be called if the city or district is not specified
        replay(reviewDao);
        _controller.setReviewDao(reviewDao);

        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "CA");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        List<IParentReviewModel> parentReviewList =
                (List) mav.getModel().get(RecentParentReviewsController.MODEL_REVIEW_LIST);

        assertNotNull(parentReviewList);
    }

}
