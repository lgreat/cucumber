/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: RecentParentReviewsControllerTest.java,v 1.8 2008/05/30 19:26:27 cpickslay Exp $
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

    public void testRecentParentReviewsController() throws Exception {
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

        List<RecentParentReviewsController.IParentReviewModel> parentReviewList =
                (List) mav.getModel().get(RecentParentReviewsController.MODEL_REVIEW_LIST);

        assertNotNull(parentReviewList);
    }
}
