/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: MySchoolListControllerTest.java,v 1.2 2006/05/31 21:44:29 apeterson Exp $
 */

package gs.web.community;

import gs.data.community.IUserDao;
import gs.data.school.ISchoolDao;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.SessionContextUtil;
import gs.web.util.AnchorListModel;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Tests SchoolsController.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
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

        List list = (List) modelAndView.getModel().get(AnchorListModel.RESULTS);
        assertEquals(4, list.size());

        // test limit
        request.setParameter(MySchoolListController.PARAM_LIMIT, "2");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        modelAndView = _controller.handleRequestInternal(request, getResponse());

        list = (List) modelAndView.getModel().get(AnchorListModel.RESULTS);
        assertEquals(3, list.size());


    }

}
