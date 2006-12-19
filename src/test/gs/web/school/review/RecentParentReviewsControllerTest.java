/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: RecentParentReviewsControllerTest.java,v 1.4 2006/12/19 01:04:17 thuss Exp $
 */

package gs.web.school.review;

import gs.data.school.ISchoolDao;
import gs.data.school.review.IReviewDao;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContextUtil;
import org.springframework.web.servlet.ModelAndView;

import java.util.Iterator;
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
        _controller.setSchoolDao((ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID));
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

        List parentReviewList =
                (List) mav.getModel().get(RecentParentReviewsController.MODEL_REVIEW_LIST);

        assertTrue(parentReviewList.size() == 10);

        for (Iterator iter = parentReviewList.iterator(); iter.hasNext();) {
            RecentParentReviewsController.IParentReviewModel review =
                    (RecentParentReviewsController.IParentReviewModel) iter.next();

            assertNotNull(review.getQuip());
            assertNotNull(review.getDate());
            assertNotNull(review.getSchoolLink());
            assertNotNull(review.getSchoolName());
            assertTrue(review.getStars() > 0);
            assertTrue(review.getStars() <= 5);
        }
    }

    public void testDateHandling() {

    }
}
