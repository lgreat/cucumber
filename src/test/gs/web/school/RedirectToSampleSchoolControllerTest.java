/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: RedirectToSampleSchoolControllerTest.java,v 1.1 2006/03/14 18:39:34 wbeck Exp $
 */
package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.MockHttpServletRequest;
import gs.web.SessionContextUtil;
import gs.data.school.School;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;

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
        MockHttpServletRequest request = getRequest();
        request.setParameter("state","CA");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, null);

        RedirectView view = (RedirectView)mav.getView();
        assertNotNull(view.getUrl());
        assertEquals("/modperl/browse_school/ca/2/", view.getUrl());
   }

   public void testGetSampleAKSchool() throws Exception {
        MockHttpServletRequest request = getRequest();
        request.setParameter("state","AK");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, null);
        RedirectView view = (RedirectView)mav.getView();
        assertNotNull(view.getUrl());

        assertEquals("/modperl/browse_school/ak/329/", view.getUrl());
   }


}