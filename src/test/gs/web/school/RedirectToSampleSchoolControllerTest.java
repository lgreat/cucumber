/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: RedirectToSampleSchoolControllerTest.java,v 1.5 2008/05/30 19:11:44 cpickslay Exp $
 */
package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContextUtil;
import static org.easymock.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Wendy Beck
 *
 */
public class RedirectToSampleSchoolControllerTest extends BaseControllerTestCase {

    private RedirectToSampleSchoolController _controller;
    private SessionContextUtil _sessionContextUtil ;
    private ISchoolDao _schoolDao;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = (RedirectToSampleSchoolController) getApplicationContext().
                getBean(RedirectToSampleSchoolController.BEAN_ID);
        _sessionContextUtil = (SessionContextUtil) getApplicationContext().
                getBean(SessionContextUtil.BEAN_ID);
        _schoolDao = createMock(ISchoolDao.class);
        _controller.setSchoolDao(_schoolDao);
    }

   public void testGetSampleCASchool() throws Exception {
       School school = new School();
       school.setId(1);
       expect(_schoolDao.getSampleSchool(State.CA)).andReturn(school);
       replay(_schoolDao);

        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state","CA");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, null);

        RedirectView view = (RedirectView)mav.getView();
        assertNotNull(view.getUrl());
        assertEquals("/modperl/browse_school/ca/1/", view.getUrl());
   }

   public void testGetSampleAKSchool() throws Exception {
       School school = new School();
       school.setId(329);
       expect(_schoolDao.getSampleSchool(State.AK)).andReturn(school);
       replay(_schoolDao);

       GsMockHttpServletRequest request = getRequest();
        request.setParameter("state","AK");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, null);
        RedirectView view = (RedirectView)mav.getView();
        assertNotNull(view.getUrl());

        assertEquals("/modperl/browse_school/ak/329/", view.getUrl());
   }


}