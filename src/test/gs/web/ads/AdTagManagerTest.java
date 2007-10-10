/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AdTagManagerTest.java,v 1.3 2007/10/10 18:41:59 dlee Exp $
 */
package gs.web.ads;

import gs.web.BaseTestCase;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Test AdTagDaoXmlConfig
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class AdTagManagerTest extends BaseTestCase {

    AdTagManager _adTagManager;

    public void setUp() throws Exception {
        super.setUp();
        _adTagManager = AdTagManager.getInstance();
    }

    public void testGetAdTag() throws IOException, ParserConfigurationException, SAXException {
        String adTag = _adTagManager.getAdTag("yahoo", AdPosition.AboveFold_Right_120x600);
        assertNotNull(adTag);
        assertTrue(adTag.indexOf("<script type=\"text/javascript\" src=\"http://us.adserver.yahoo.com/a?f=96345362&p=ed&l=SKY&c=r\">") != -1);
        _log.debug(adTag);
    }

    public void testSingleton() {
        AdTagManager adTagManager2 = AdTagManager.getInstance();
        assertTrue(_adTagManager == adTagManager2);
    }

    public void testCobrandKey() {
        AdTagManager adTagManager = AdTagManager.getInstance();
        assertEquals("yahoox20", adTagManager.getCobrandKey("yahoo",AdPosition.X_20));
        assertEquals("yahoox20", adTagManager.getCobrandKey("yahooed",AdPosition.X_20));
        assertEquals("sfgatex20", adTagManager.getCobrandKey("sfgate",AdPosition.X_20));
    }
}
