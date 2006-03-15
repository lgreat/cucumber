/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ListModelSaTest.java,v 1.1 2006/03/15 02:24:21 apeterson Exp $
 */

package gs.web.util;

import junit.framework.TestCase;

import java.util.List;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class ListModelSaTest extends TestCase {

    public void testBasic() {
        ListModel model = new ListModel();
        model.addResult(new Anchor("http://ndpsoftware.com", "NDP Software"));
        model.addResult(new Anchor("http://greatschools.net", "Great Schools"));
        model.setHeading("Great Web Sites!");

        List results = model.getResults();
        Anchor a = (Anchor) results.get(0);
        assertEquals("NDP Software", a.getContents());
        a = (Anchor) results.get(1);
        assertEquals("http://greatschools.net", a.getHref());

        assertEquals("Great Web Sites!", model.getHeading());

        // other constructor
        model = new ListModel("Not much");
        assertEquals("Not much", model.getHeading());
    }
}
