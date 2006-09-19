/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AdTagManagerTest.java,v 1.1 2006/09/19 23:31:10 dlee Exp $
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
        String adTag = _adTagManager.getAdTag("yahoo", AdPosition.X_20);
        assertNotNull(adTag);
        assertTrue(adTag.indexOf("<script type=\"text/javascript\" src=\"http://us.adserver.yahoo.com/a?f=96345362&p=ed&l=SKY&c=r\">") != -1);
        _log.debug(adTag);
    }

    public void testSingleton() {
        AdTagManager adTagManager2 = AdTagManager.getInstance();
        assertTrue(_adTagManager == adTagManager2);
    }
}
