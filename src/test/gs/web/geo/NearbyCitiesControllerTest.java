/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: NearbyCitiesControllerTest.java,v 1.1 2006/04/07 17:35:39 apeterson Exp $
 */

package gs.web.geo;

import gs.data.geo.IGeoDao;
import gs.web.BaseControllerTestCase;
import gs.web.SessionContextUtil;

/**
 * Provides tests for NearbyCitiesController.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class NearbyCitiesControllerTest extends BaseControllerTestCase {

    private NearbyCitiesController _controller;
    private SessionContextUtil _sessionContextUtil;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new NearbyCitiesController();
        _controller.setApplicationContext(getApplicationContext());
        _controller.setGeoDao((IGeoDao) getApplicationContext().getBean(IGeoDao.BEAN_ID));
        _sessionContextUtil = (SessionContextUtil) getApplicationContext().
                getBean(SessionContextUtil.BEAN_ID);
    }

    public void testNothing() {
        
    }
}
