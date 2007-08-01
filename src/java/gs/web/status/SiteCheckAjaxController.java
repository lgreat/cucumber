package gs.web.status;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.web.util.Crawler;
import gs.web.util.IPageVisitor;
import gs.web.util.Page;

import java.io.Writer;
import java.io.IOException;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SiteCheckAjaxController extends AbstractController {

    public static final String BEAN_ID = "/status/scAjax.page";
    private Crawler _crawler;

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        String root = (String)request.getParameter("root_url");
        _crawler.addPageVisitor(new WebVisitor(response.getWriter()));
        _crawler.crawl(root);
        return null;
    }

    public Crawler getCrawler() {
        return _crawler;
    }

    public void setCrawler(Crawler crawler) {
        _crawler = crawler;
    }
}

class WebVisitor implements IPageVisitor {

    private Writer _writer;
    public WebVisitor(Writer writer) {
        this._writer = writer;
    }
    public void visit(Page page) {
        try {
            _writer.write("<div>" + page.getUrl().toString() + "</div>");
            _writer.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}