/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolsControllerTest.java,v 1.1 2006/03/22 01:40:54 apeterson Exp $
 */

package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.SessionContextUtil;
import gs.web.search.ResultsPager;
import gs.data.school.district.IDistrictDao;
import gs.data.search.Searcher;

/**
 * Tests SchoolsControllerTest.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class SchoolsControllerTest extends BaseControllerTestCase {

    private SchoolsController _controller;
    private SessionContextUtil _sessionContextUtil;

    protected void setUp() throws Exception {
        super.setUp();

        _controller = new SchoolsController();
        _controller.setApplicationContext(getApplicationContext());
        _controller.setDistrictDao((IDistrictDao) getApplicationContext().getBean(IDistrictDao.BEAN_ID));
        _controller.setResultsPager((ResultsPager) getApplicationContext().getBean(ResultsPager.BEAN_ID));
        _controller.setSearcher((Searcher) getApplicationContext().getBean(Searcher.BEAN_ID));

        _sessionContextUtil = (SessionContextUtil) getApplicationContext().getBean(SessionContextUtil.BEAN_ID);
    }

    public void testByCity() {

    }

    public void testByDistrict() {

    }

}
