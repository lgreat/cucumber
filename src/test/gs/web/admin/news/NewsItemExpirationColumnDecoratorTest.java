/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.admin.news;

import gs.web.BaseTestCase;
import gs.web.jsp.MockPageContext;

import java.util.Calendar;

import org.displaytag.exception.DecoratorException;

/**
 * Provides testing for the NewsItemExpirationColumnDecorator class.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class NewsItemExpirationColumnDecoratorTest extends BaseTestCase {
    private NewsItemExpirationColumnDecorator decorator;

    public void setUp() throws Exception {
        super.setUp();
        decorator = new NewsItemExpirationColumnDecorator();
    }

    public void testDateNormal() throws DecoratorException {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Object rval = decorator.decorate(cal.getTime(), new MockPageContext(), null);
        assertNotNull(rval);
        String span = (String) rval;
        assertEquals(-1, span.indexOf(NewsItemExpirationColumnDecorator.EXPIRED_DATE_CLASS_NAME));
    }

    public void testDateExpired() throws DecoratorException {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Object rval = decorator.decorate(cal.getTime(), new MockPageContext(), null);
        assertNotNull(rval);
        String span = (String) rval;
        assertTrue(span.indexOf(NewsItemExpirationColumnDecorator.EXPIRED_DATE_CLASS_NAME) > -1);
    }
}
