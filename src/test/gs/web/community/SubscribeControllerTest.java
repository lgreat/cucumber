/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SubscribeControllerTest.java,v 1.2 2006/04/28 06:39:18 apeterson Exp $
 */

package gs.web.community;

import gs.data.community.IUserDao;
import gs.data.community.PurchaseManager;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.SessionContextUtil;

/**
 * Tests SchoolsController.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class SubscribeControllerTest extends BaseControllerTestCase {

    private SubscribeController _controller;
    private SessionContextUtil _sessionContextUtil;

    protected void setUp() throws Exception {
        super.setUp();

        _controller = new SubscribeController();
        _controller.setApplicationContext(getApplicationContext());
        _controller.setUserDao((IUserDao) getApplicationContext().getBean(IUserDao.BEAN_ID));
        _controller.setPurchaseManager((PurchaseManager) getApplicationContext().getBean(PurchaseManager.BEAN_ID));

        _sessionContextUtil = (SessionContextUtil) getApplicationContext().getBean(SessionContextUtil.BEAN_ID);
    }

    public void testSubscribeController() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        request.setParameter(SubscribeController.EMAIL_PARAM, "eford@greatschools.net");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        Object o = _controller.formBackingObject(request);


    }

}
