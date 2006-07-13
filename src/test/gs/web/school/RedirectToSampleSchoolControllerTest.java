/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: RedirectToSampleSchoolControllerTest.java,v 1.3 2006/07/13 07:54:00 apeterson Exp $
 */
package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContextUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Wendy Beck
 *
 */
public class RedirectToSampleSchoolControllerTest extends BaseControllerTestCase {

    private RedirectToSampleSchoolController _controller;
    private SessionContextUtil _sessionContextUtil ;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = (RedirectToSampleSchoolController) getApplicationContext().
                getBean(RedirectToSampleSchoolController.BEAN_ID);
        _sessionContextUtil = (SessionContextUtil) getApplicationContext().
                getBean(SessionContextUtil.BEAN_ID);
    }

   public void testGetSampleCASchool() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state","CA");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, null);

        RedirectView view = (RedirectView)mav.getView();
        assertNotNull(view.getUrl());
        assertEquals("/modperl/browse_school/ca/2/", view.getUrl());
   }

   public void testGetSampleAKSchool() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state","AK");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, null);
        RedirectView view = (RedirectView)mav.getView();
        assertNotNull(view.getUrl());

        assertEquals("/modperl/browse_school/ak/329/", view.getUrl());
   }


}