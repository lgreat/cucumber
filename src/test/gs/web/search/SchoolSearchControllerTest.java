package gs.web.search;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.PageHelper;
import junit.framework.TestCase;
import org.apache.commons.collections.MultiMap;

import java.util.*;

public class SchoolSearchControllerTest extends BaseControllerTestCase {
    private SchoolSearchController _controller;
    public void setUp() throws Exception {
        super.setUp();
        _controller = new SchoolSearchController();

        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
    }
    public void testAddGamAttributes() {
        Map<FieldConstraint,String> constraints = new HashMap<FieldConstraint,String>();
        List<FieldFilter> filters = new ArrayList<FieldFilter>();
        PageHelper referencePageHelper;
        PageHelper actualPageHelper;

        // basic case: null checks
        boolean threwException = false;
        try {
            _controller.addGamAttributes(null, null, null);
        } catch (IllegalArgumentException e) {
            threwException = true;
        }
        assertTrue(threwException);

        // school type

        filters.add(FieldFilter.SchoolTypeFilter.PUBLIC);
        filters.add(FieldFilter.SchoolTypeFilter.CHARTER);

        actualPageHelper = new PageHelper(_sessionContext, _request);
        _controller.addGamAttributes(actualPageHelper, constraints, filters);

        referencePageHelper = new PageHelper(_sessionContext, _request);
        referencePageHelper.addAdKeywordMulti("type","public");
        referencePageHelper.addAdKeywordMulti("type","charter");

        Collection actualTypeKeywords = (Collection)actualPageHelper.getAdKeywords().get("type");
        assertNotNull(actualTypeKeywords);
        assertEquals(2,actualTypeKeywords.size());
        assertTrue(actualTypeKeywords.contains("public"));
        assertTrue(actualTypeKeywords.contains("charter"));

        // district browse

        filters.clear();
        constraints.clear();

        constraints.put(FieldConstraint.DISTRICT_ID, "3");

        actualPageHelper = new PageHelper(_sessionContext, _request);
        _controller.addGamAttributes(actualPageHelper, constraints, filters);

        referencePageHelper = new PageHelper(_sessionContext, _request);
        referencePageHelper.addAdKeyword("district_name","San Francisco Unified School District");
        referencePageHelper.addAdKeyword("district_id","3");

        Collection actualDistrictIdKeywords = (Collection)actualPageHelper.getAdKeywords().get("district_id");
        assertNotNull(actualDistrictIdKeywords);
        assertEquals(1,actualDistrictIdKeywords.size());
        // TODO:fixme
        //assertEquals("3", (String)actualDistrictIdKeywords.get(0));
/*
        Collection actualDistrictNameKeywords = (Collection)actualPageHelper.getAdKeywords().get("district_name");
        assertNotNull(actualDistrictNameKeywords);
        assertEquals(1,actualDistrictNameKeywords.size());
*/

/*
        referencePageHelper.addAdKeyword("state", "CA");
        referencePageHelper.addAdKeywordMulti("editorial", "Category 1");
        referencePageHelper.addAdKeywordMulti("editorial", "Category 2");
        referencePageHelper.addAdKeywordMulti("editorial", "Category 3");
        referencePageHelper.addAdKeywordMulti("editorial", "2ndCat1");
        referencePageHelper.addAdKeywordMulti("editorial", "2ndCat2");
        referencePageHelper.addAdKeywordMulti("editorial", "2ndCatA");
        referencePageHelper.addAdKeywordMulti("editorial", "2ndCatB");
        referencePageHelper.addAdKeyword("article_id", "23");

        Collection referenceEditorialKeywords = (Collection)referencePageHelper.getAdKeywords().get("editorial");
        Collection actualEditorialKeywords = (Collection)pageHelper.getAdKeywords().get("editorial");
        assertEquals(referenceEditorialKeywords.size(), actualEditorialKeywords.size());
*/
    }

    public void testGetGradeLevelFilters() {
        String[] gradeLevels = new String[] {"p","h", "blah"};

        List<FieldFilter> filters = _controller.getGradeLevelFilters(gradeLevels);

        assertEquals("Filters list should contain two filters", 2, filters.size());

        assertTrue(filters.contains(FieldFilter.GradeLevelFilter.PRESCHOOL));
        assertTrue(filters.contains(FieldFilter.GradeLevelFilter.HIGH));
    }

}
