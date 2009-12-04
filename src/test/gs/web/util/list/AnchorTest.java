/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: AnchorTest.java,v 1.2 2009/12/04 20:54:12 npatury Exp $
 */

package gs.web.util.list;

import junit.framework.TestCase;
import gs.web.util.list.Anchor;

/**
 * Tests Anchor objects.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class AnchorTest extends TestCase {

    public void testImages() {
        Anchor a = new Anchor("/here.html", "Click Here Please", null, "/img/res/bigOrangeFinger.gif");
        assertEquals("/here.html", a.getHref());
        assertEquals("/here.html", a.getHrefXml());
        assertEquals("Click Here Please", a.getContents());
        assertEquals("/img/res/bigOrangeFinger.gif", a.getImage());
    }

    public void testAsATag() {
        Anchor a = new Anchor("/here.html", "Click Here Please", null, "/img/res/bigOrangeFinger.gif");

        assertEquals("<a href=\"/here.html\">Click Here Please</a>", a.asATag());
    }

}
