package gs.web.mvc;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller maps wildcard path patterns to views.  This allows multiple pages to be
 * handled by this a single controller when, for example, an entire directory of pages can
 * be served without any controller backing.  This saves you from having to create a new
 * ParameterizableViewController bean for each jspx page.
 *
 * This controller supports sub-directories, so for example, the following pages-servlet.xml
 * config:
 *
 * <pre>
 *    <bean name="/api/docs/*"
 *         class="gs.web.mvc.WildcardViewController" lazy-init="true">
 *       <property name="basePath" value="api/docs"/>
 *   </bean>
 * </pre>
 *
 * will resolve "api/docs/test.page" as well as "api/docs/sub1/sub2/test.page"
 * 
 * Created by chriskimm@greatschools.org
 */
public class WildcardViewController extends AbstractController {

    // Required parameter
    private String _basePath;

    public String getBasePath() {
        return _basePath;
    }

    public void setBasePath(String basePath) {
        _basePath = basePath;
    }

    protected void initApplicationContext() {
        if (this._basePath == null) {
            throw new IllegalArgumentException("Property 'basePath' is required");
        }
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        String uri = request.getRequestURI();

        int basePathStart = uri.lastIndexOf(_basePath);
        int basePathEnd = basePathStart + _basePath.length();
        int lastDot = uri.lastIndexOf('.');

        String page;
        if (lastDot >= basePathEnd) {
               page = uri.substring(basePathEnd, lastDot);
        } else {
               page = uri.substring(basePathEnd);
        }

        StringBuilder view = new StringBuilder().append(_basePath).append(page);
        return new ModelAndView(view.toString());
    }
}
