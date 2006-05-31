/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AnchorListModelSaTest.java,v 1.1 2006/05/31 21:44:29 apeterson Exp $
 */

package gs.web.util;

import junit.framework.TestCase;

import java.util.List;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class AnchorListModelSaTest extends TestCase {

    public void testBasic() {
        AnchorListModel model = new AnchorListModel();
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
        model = new AnchorListModel("Not much");
        assertEquals("Not much", model.getHeading());
    }
}
