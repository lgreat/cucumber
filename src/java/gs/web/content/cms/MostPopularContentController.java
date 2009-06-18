package gs.web.content.cms;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import gs.data.content.cms.CmsLink;

public class MostPopularContentController extends AbstractController {
    private static final Log _log = LogFactory.getLog(MostPopularContentController.class);
    private String _viewName;

    final private static String MODEL_LINKS = "links";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        List<CmsLink> links = new ArrayList<CmsLink>();

        // TODO: populate links with the actual links from the CMS
        // TODO: remove hard-coding of sample links
        CmsLink link = new CmsLink();
        link.setUrl("http://www.google.com");
        link.setLinkText("Lorem ipsum lorem ipsum lorem ipsum 1");
        links.add(link);
        link = new CmsLink();
        link.setUrl("http://www.yahoo.com");
        link.setLinkText("Lorem ipsum lorem ipsum lorem ipsum 2");
        links.add(link);
        link = new CmsLink();
        link.setUrl("http://www.jquery.org");
        link.setLinkText("Lorem ipsum lorem ipsum lorem ipsum 3");
        links.add(link);
        link = new CmsLink();
        link.setUrl("http://www.greatschools.net");
        link.setLinkText("Lorem ipsum lorem ipsum lorem ipsum 4");
        links.add(link);
        link = new CmsLink();
        link.setUrl("http://www.loremipsum.net");
        link.setLinkText("Lorem ipsum lorem ipsum lorem ipsum 5");
        links.add(link);

        model.put(MODEL_LINKS, links);

        return new ModelAndView(_viewName, model);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
