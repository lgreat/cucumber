/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: RedirectToSampleSchoolControllerTest.java,v 1.7 2009/12/04 20:54:15 npatury Exp $
 */
package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.util.Address;
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
       school.setName("Alameda High School");
       Address address = new Address("123 way", "CityName", State.CA, "12345");
       school.setPhysicalAddress(address);

       expect(_schoolDao.getSampleSchool(State.CA)).andReturn(school);
       replay(_schoolDao);

        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state","CA");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, null);

        RedirectView view = (RedirectView)mav.getView();
        assertNotNull(view.getUrl());
        assertEquals("/california/cityname/1-Alameda-High-School/", view.getUrl());
   }

   public void testGetSampleAKSchool() throws Exception {
       School school = new School();
       school.setId(329);
       school.setName("Alaska High School");
       Address address = new Address("123 way", "CityName", State.AK, "12345");
       school.setPhysicalAddress(address);

       expect(_schoolDao.getSampleSchool(State.AK)).andReturn(school);
       replay(_schoolDao);

       GsMockHttpServletRequest request = getRequest();
        request.setParameter("state","AK");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, null);
        RedirectView view = (RedirectView)mav.getView();
        assertNotNull(view.getUrl());

        assertEquals("/alaska/cityname/329-Alaska-High-School/", view.getUrl());
   }


}