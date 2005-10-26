/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: StateControllerSaTest.java,v 1.1 2005/10/26 20:51:33 apeterson Exp $
 */

package gs.web.state;

import gs.data.school.district.IDistrictDao;
import gs.web.BaseControllerTestCase;
import gs.web.util.Anchor;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class StateControllerSaTest extends BaseControllerTestCase {

    public void testTopDistrictsController() throws Exception {
        TopDistrictsController c = new TopDistrictsController();
        c.setApplicationContext(getApplicationContext());
        c.setDistrictDao((IDistrictDao) getApplicationContext().getBean(IDistrictDao.BEAN_ID));
        c.setViewName("/resultList.jspx");

        ModelAndView modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        final Object header = modelAndView.getModel().get("header");
        assertNotNull(header);
        assertTrue(header instanceof String);
        assertEquals("California Districts", header);
        final List results = (List) modelAndView.getModel().get("results");
        assertNotNull(results);
        assertEquals(6, results.size());
        Anchor first = (Anchor) results.get(0);
        assertEquals("Fresno", first.getContents());
        Anchor last = (Anchor) results.get(4);
        assertEquals("San Francisco", last.getContents());
        assertEquals("/cgi-bin/ca/district_profile/717", last.getHref());
        assertNotNull(modelAndView.getModel().get("results"));

        Anchor veryLast = (Anchor) results.get(5);
        assertEquals("/modperl/distlist/CA", veryLast.getHref());
        assertEquals("View all California districts", veryLast.getContents());
    }


}
