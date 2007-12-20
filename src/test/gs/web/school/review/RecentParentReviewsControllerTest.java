/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: RecentParentReviewsControllerTest.java,v 1.7 2007/12/20 22:06:09 aroy Exp $
 */

package gs.web.school.review;

import gs.data.school.review.IReviewDao;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContextUtil;
import org.springframework.web.servlet.ModelAndView;

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

        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "AK");
        request.setParameter("city", "Anchorage");
        request.setParameter(RecentParentReviewsController.PARAM_MAX, "10");
        _sessionContextUtil.prepareSessionContext(request, getResponse());        

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        List<RecentParentReviewsController.IParentReviewModel> parentReviewList =
                (List) mav.getModel().get(RecentParentReviewsController.MODEL_REVIEW_LIST);

        assertTrue(parentReviewList.size() == 10);

        for (RecentParentReviewsController.IParentReviewModel review : parentReviewList) {
            assertNotNull(review.getQuip());
            assertTrue("Expect quip to be no more than 90 characters long: \"" + review.getQuip() + 
                    "\", actual length " + review.getQuip().length(),
                    review.getQuip().length() < 91);
            assertNotNull(review.getDate());
            assertNotNull(review.getSchoolName());
            assertNotNull(review.getSchool());
            assertTrue(review.getStars() > 0);
            assertTrue(review.getStars() <= 5);
        }
    }
}
