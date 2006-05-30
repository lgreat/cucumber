package gs.web.util;

import gs.web.BaseControllerTestCase;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author <a href="mailto:thuss@greatschools.net">Todd Huss</a>
 */
public class HttpHeaderFilterTest extends BaseControllerTestCase {

    public void testHeaderFilter() throws IOException, ServletException {
        HttpHeaderFilter headerFilter = new HttpHeaderFilter();
        ServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        MockFilterChain chain = new MockFilterChain();
        headerFilter.init(new MockFilterConfig());
        headerFilter.doFilter(request, response, chain);
        assert(response.containsHeader(MockFilterConfig.PARAM));
    }

    protected class MockFilterConfig implements FilterConfig {

        Hashtable params = new Hashtable();

        public static final String PARAM = "Cache-Control";

        public static final String VALUE = "max-age=3600";

        MockFilterConfig() {
            params.put(PARAM, VALUE);
        }

        public String getFilterName() {
            return null;
        }

        public ServletContext getServletContext() {
            return null;
        }

        public String getInitParameter(String name) {
            return (String)params.get(name);
        }

        public Enumeration getInitParameterNames() {
            return params.keys();
        }
    }


}

