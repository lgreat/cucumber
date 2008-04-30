/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: MySchoolListControllerTest.java,v 1.4 2008/04/30 21:02:42 chriskimm Exp $
 */

package gs.web.community;

import gs.data.community.IUserDao;
import gs.data.school.ISchoolDao;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.AnchorListModel;
import gs.web.util.context.SessionContextUtil;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Set;

/**
 * Tests MySchoolListController.
 */
public class MySchoolListControllerTest extends BaseControllerTestCase {

    private MySchoolListController _controller;
    private SessionContextUtil _sessionContextUtil;

    protected void setUp() throws Exception {
        super.setUp();

        _controller = new MySchoolListController();
        _controller.setApplicationContext(getApplicationContext());
        _controller.setUserDao((IUserDao) getApplicationContext().getBean(IUserDao.BEAN_ID));
        _controller.setSchoolDao((ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID));

        _sessionContextUtil = (SessionContextUtil) getApplicationContext().getBean(SessionContextUtil.BEAN_ID);
    }

    public void testMySchoolListController() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        request.setParameter(SessionContextUtil.MEMBER_PARAM, "1");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        ModelAndView modelAndView = _controller.handleRequestInternal(request, getResponse());
        Set schools = (Set) modelAndView.getModel().get("schools");
        assertEquals(4, schools.size());
    }
}
