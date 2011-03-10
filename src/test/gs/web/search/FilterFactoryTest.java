package gs.web.search;

import junit.framework.TestCase;

public class FilterFactoryTest extends TestCase {
    FilterFactory _filterFactory;

    public void setUp() throws Exception {
        _filterFactory = new FilterFactory();
    }

    public void testCreateFilterGroup() throws Exception {

    }

    public void testValueOf() throws Exception {

        FieldFilter filter = FieldFilter.SchoolTypeFilter.PUBLIC;

        FieldFilter f = _filterFactory.valueOf(filter, "private");

        assertEquals("FilterFactory should return correct filter", FieldFilter.SchoolTypeFilter.PRIVATE, f);
    }

    public void testValueOf2() throws Exception {

        String name = "SchoolTypeFilter";

        FieldFilter f = _filterFactory.valueOf(name, "private");

        assertEquals("FilterFactory should return correct filter", FieldFilter.SchoolTypeFilter.PRIVATE, f);
    }
}
