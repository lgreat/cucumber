/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: RecentParentReviewsControllerTest.java,v 1.2 2006/03/24 23:18:19 apeterson Exp $
 */

package gs.web.school.review;

import gs.data.school.ISchoolDao;
import gs.data.school.review.IReviewDao;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.SessionContextUtil;
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
    }

    public void testRecentParentReviewsController() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "AK");
        request.setParameter("city", "Anchorage");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        List parentReviewList =
                (List) mav.getModel().get(RecentParentReviewsController.MODEL_REVIEW_LIST);

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
