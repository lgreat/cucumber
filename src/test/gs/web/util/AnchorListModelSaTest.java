/*
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: AnchorListModelSaTest.java,v 1.5 2009/12/04 22:27:04 chriskimm Exp $
 */

package gs.web.util;

import junit.framework.TestCase;

import java.util.List;

import gs.web.util.list.Anchor;
import gs.web.util.list.AnchorListModel;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 */
public class AnchorListModelSaTest extends TestCase {

    public void testBasic() {
        AnchorListModel model = new AnchorListModel();
        model.add(new Anchor("http://ndpsoftware.com", "NDP Software"));
        model.add(new Anchor("http://greatschools.org", "Great Schools"));
        model.setHeading("Great Web Sites!");

        List results = model.getResults();
        Anchor a = (Anchor) results.get(0);
        assertEquals("NDP Software", a.getContents());
        a = (Anchor) results.get(1);
        assertEquals("http://greatschools.org", a.getHref());

        assertEquals("Great Web Sites!", model.getHeading());

        // other constructor
        model = new AnchorListModel("Not much");
        assertEquals("Not much", model.getHeading());
    }
}
