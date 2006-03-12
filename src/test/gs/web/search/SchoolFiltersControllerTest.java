package gs.web.search;

import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolFiltersControllerTest extends BaseControllerTestCase {

    public void testLevels () throws Exception {
        SchoolFiltersController controller = new SchoolFiltersController();

        getRequest().setQueryString("c=school&state=ca&city=South%20San%20Francisco");
        ModelAndView mAndV_noGL =
                controller.handleRequestInternal(getRequest(), getResponse());
        List levels_noGL = (List)mAndV_noGL.getModel().get("levels");
        assertEquals("<a href=\"/search/search.page?c=school&state=ca&city=South%20San%20Francisco&gl=elementary\">Elementary</a>", levels_noGL.get(0));
        assertEquals("<a href=\"/search/search.page?c=school&state=ca&city=South%20San%20Francisco&gl=middle\">Middle</a>", levels_noGL.get(1));
        assertEquals("<a href=\"/search/search.page?c=school&state=ca&city=South%20San%20Francisco&gl=high\">High</a>", levels_noGL.get(2));


        getRequest().addParameter("gl", "elementary");
        getRequest().addParameter("gl", "middle");
        getRequest().addParameter("gl", "high");
        getRequest().setQueryString("c=school&state=ca&city=South%20San%20Francisco&gl=elementary&gl=middle&gl=high");
        ModelAndView mAndV =
                controller.handleRequestInternal(getRequest(), getResponse());
        List levels = (List)mAndV.getModel().get("levels");
        assertEquals("Elementary (<a href=\"/search/search.page?c=school&state=ca&city=South%20San%20Francisco&gl=middle&gl=high\">remove</a>)", levels.get(0));
        assertEquals("Middle (<a href=\"/search/search.page?c=school&state=ca&city=South%20San%20Francisco&gl=elementary&gl=high\">remove</a>)", levels.get(1));
        assertEquals("High (<a href=\"/search/search.page?c=school&state=ca&city=South%20San%20Francisco&gl=elementary&gl=middle\">remove</a>)", levels.get(2));

        List types = (List)mAndV.getModel().get("types");
        assertEquals("<a href=\"/search/search.page?c=school&state=ca&city=South%20San%20Francisco&gl=elementary&gl=middle&gl=high&st=public\">Public</a>", types.get(0));
        assertEquals("<a href=\"/search/search.page?c=school&state=ca&city=South%20San%20Francisco&gl=elementary&gl=middle&gl=high&st=charter\">Charter</a>", types.get(1));
        assertEquals("<a href=\"/search/search.page?c=school&state=ca&city=South%20San%20Francisco&gl=elementary&gl=middle&gl=high&st=private\">Private</a>", types.get(2));
    }

    public void testTypes() throws Exception {
        SchoolFiltersController controller = new SchoolFiltersController();

        getRequest().setQueryString("c=school&state=ca&city=South%20San%20Francisco&gl=elementary");
        ModelAndView mAndV = controller.handleRequestInternal(getRequest(), getResponse());
        List types = (List)mAndV.getModel().get("types");
        assertEquals("<a href=\"/search/search.page?c=school&state=ca&city=South%20San%20Francisco&gl=elementary&st=public\">Public</a>", types.get(0));
        assertEquals("<a href=\"/search/search.page?c=school&state=ca&city=South%20San%20Francisco&gl=elementary&st=charter\">Charter</a>", types.get(1));
        assertEquals("<a href=\"/search/search.page?c=school&state=ca&city=South%20San%20Francisco&gl=elementary&st=private\">Private</a>", types.get(2));

        getRequest().setQueryString("c=school&state=ca&city=South%20San%20Francisco&gl=elementary&st=public&st=charter&st=private");
        getRequest().addParameter("st", "public");
        getRequest().addParameter("st", "charter");
        getRequest().addParameter("st", "private");
        ModelAndView mAndV2 = controller.handleRequestInternal(getRequest(), getResponse());
        List types2 = (List)mAndV2.getModel().get("types");
        assertEquals("Public (<a href=\"/search/search.page?c=school&state=ca&city=South%20San%20Francisco&gl=elementary&st=charter&st=private\">remove</a>)", types2.get(0));
        assertEquals("Charter (<a href=\"/search/search.page?c=school&state=ca&city=South%20San%20Francisco&gl=elementary&st=public&st=private\">remove</a>)", types2.get(1));
        assertEquals("Private (<a href=\"/search/search.page?c=school&state=ca&city=South%20San%20Francisco&gl=elementary&st=public&st=charter\">remove</a>)", types2.get(2));
    }
}
