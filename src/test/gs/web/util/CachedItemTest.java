package gs.web.util;

import gs.web.BaseTestCase;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CachedItemTest extends BaseTestCase {
    private CachedItem<Object> _item;

    public void setUp() throws Exception {
        super.setUp();

        _item = new CachedItem<Object>();
    }

    public void testBasics() {
        long currentTime = System.currentTimeMillis();
        long fiveMinsAgo = currentTime - 300000;
        long tenMins = 600000;

        _item.setCachedAt(fiveMinsAgo);
        _item.setCacheDuration(tenMins);
        Object o = new Object();
        _item.setData(o);

        assertEquals(tenMins, _item.getCacheDuration());
        assertEquals(fiveMinsAgo, _item.getCachedAt());
        assertSame(o, _item.getData());
    }

    public void testGetDataIfNotExpired() {

        long currentTime = System.currentTimeMillis();
        long fiveMinsAgo = currentTime - 300000;
        long tenMins = 600000;
        long twoMins = 120000;

        _item.setCachedAt(fiveMinsAgo);
        _item.setCacheDuration(tenMins);
        Object o = new Object();
        _item.setData(o);

        assertNotNull(_item.getDataIfNotExpired());
        assertSame(o, _item.getDataIfNotExpired());

        _item.setCacheDuration(twoMins);
        assertNull(_item.getDataIfNotExpired());
        assertNotNull(_item.getData());
        assertSame(o, _item.getData());
    }
}
