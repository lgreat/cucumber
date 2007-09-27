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
        assertEquals("<a href=\"/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=e\" class=\"noInterstitial\">Elementary</a>", levels_noGL.get(0));
        assertEquals("<a href=\"/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=m\" class=\"noInterstitial\">Middle</a>", levels_noGL.get(1));
        assertEquals("<a href=\"/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=h\" class=\"noInterstitial\">High</a>", levels_noGL.get(2));


        getRequest().addParameter(SchoolFiltersController.PARAM_LEVEL_CODE, "e");
        getRequest().addParameter(SchoolFiltersController.PARAM_LEVEL_CODE, "m");
        getRequest().addParameter(SchoolFiltersController.PARAM_LEVEL_CODE, "h");
        getRequest().setQueryString("c=school&state=ca&city=South%20San%20Francisco&lc=e&lc=m&lc=h");
        ModelAndView mAndV =
                controller.handleRequestInternal(getRequest(), getResponse());
        List levels = (List)mAndV.getModel().get("levels");
        assertEquals("Elementary (<a href=\"/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=m&amp;lc=h\" class=\"noInterstitial\">remove</a>)", levels.get(0));
        assertEquals("Middle (<a href=\"/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=e&amp;lc=h\" class=\"noInterstitial\">remove</a>)", levels.get(1));
        assertEquals("High (<a href=\"/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=e&amp;lc=m\" class=\"noInterstitial\">remove</a>)", levels.get(2));

        List types = (List)mAndV.getModel().get("types");
        assertEquals("<a href=\"/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=e&amp;lc=m&amp;lc=h&amp;st=public\" class=\"noInterstitial\">Public</a>", types.get(0));
        assertEquals("<a href=\"/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=e&amp;lc=m&amp;lc=h&amp;st=charter\" class=\"noInterstitial\">Charter</a>", types.get(1));
        assertEquals("<a href=\"/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=e&amp;lc=m&amp;lc=h&amp;st=private\" class=\"noInterstitial\">Private</a>", types.get(2));
    }

    public void testTypes() throws Exception {
        SchoolFiltersController controller = new SchoolFiltersController();

        getRequest().setQueryString("c=school&state=ca&city=South%20San%20Francisco&lc=e");
        ModelAndView mAndV = controller.handleRequestInternal(getRequest(), getResponse());
        List types = (List)mAndV.getModel().get("types");
        assertEquals("<a href=\"/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=e&amp;st=public\" class=\"noInterstitial\">Public</a>", types.get(0));
        assertEquals("<a href=\"/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=e&amp;st=charter\" class=\"noInterstitial\">Charter</a>", types.get(1));
        assertEquals("<a href=\"/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=e&amp;st=private\" class=\"noInterstitial\">Private</a>", types.get(2));

        getRequest().setQueryString("c=school&state=ca&city=South%20San%20Francisco&lc=e&st=public&st=charter&st=private");
        getRequest().addParameter("st", "public");
        getRequest().addParameter("st", "charter");
        getRequest().addParameter("st", "private");
        ModelAndView mAndV2 = controller.handleRequestInternal(getRequest(), getResponse());
        List types2 = (List)mAndV2.getModel().get("types");
        assertEquals("Public (<a href=\"/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=e&amp;st=charter&amp;st=private\" class=\"noInterstitial\">remove</a>)", types2.get(0));
        assertEquals("Charter (<a href=\"/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=e&amp;st=public&amp;st=private\" class=\"noInterstitial\">remove</a>)", types2.get(1));
        assertEquals("Private (<a href=\"/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=e&amp;st=public&amp;st=charter\" class=\"noInterstitial\">remove</a>)", types2.get(2));
    }

    public void testContext() throws Exception {
        SchoolFiltersController controller = new SchoolFiltersController();

        getRequest().setQueryString("c=school&state=ca&city=South%20San%20Francisco&lc=e");
        getRequest().setContextPath("/gs-web");
        ModelAndView mAndV = controller.handleRequestInternal(getRequest(), getResponse());
        List types = (List)mAndV.getModel().get("types");
        assertEquals("<a href=\"/gs-web/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=e&amp;st=public\" class=\"noInterstitial\">Public</a>", types.get(0));
        assertEquals("<a href=\"/gs-web/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=e&amp;st=charter\" class=\"noInterstitial\">Charter</a>", types.get(1));
        assertEquals("<a href=\"/gs-web/schools.page?c=school&amp;state=ca&amp;city=South%20San%20Francisco&amp;lc=e&amp;st=private\" class=\"noInterstitial\">Private</a>", types.get(2));

    }

    public void testDest() throws Exception {
        SchoolFiltersController controller = new SchoolFiltersController();

        getRequest().setQueryString("state=CA&city=X&dest=/xxx.page");
        getRequest().setParameter("dest", "/xxx.page");
        ModelAndView mAndV = controller.handleRequestInternal(getRequest(), getResponse());
        List types = (List)mAndV.getModel().get("types");
        assertEquals("<a href=\"/xxx.page?state=CA&amp;city=X&amp;st=public\" class=\"noInterstitial\">Public</a>", types.get(0));
        assertEquals("<a href=\"/xxx.page?state=CA&amp;city=X&amp;st=charter\" class=\"noInterstitial\">Charter</a>", types.get(1));
        assertEquals("<a href=\"/xxx.page?state=CA&amp;city=X&amp;st=private\" class=\"noInterstitial\">Private</a>", types.get(2));

    }


}
