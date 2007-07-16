package gs.web.util;

import gs.web.BaseControllerTestCase;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * We disable jsessionid URL rewriting by overloading the encode methods on HttpServletResponse
 *
 * @author <a href="mailto:thuss@greatschools.net">Todd Huss</a>
 */
public class DisableUrlSessionFilterTest extends BaseControllerTestCase {

    public void testDisableUrlSessionFilter() throws IOException, ServletException {
        DisableUrlSessionFilter filter = new DisableUrlSessionFilter();
        ServletRequest request = getRequest();

        // We create a URL munging response object so the unit test can verify these methods never get called
        HttpServletResponseWrapper response = new HttpServletResponseWrapper(getResponse()) {
            public String encodeRedirectUrl(String url) {
                return url + "jsessionID";
            }

            public String encodeRedirectURL(String url) {
                return url + "jsessionID";
            }

            public String encodeUrl(String url) {
                return url + "jsessionID";
            }

            public String encodeURL(String url) {
                return url + "jsessionID";
            }
        };
        // Assertions happen in the inner FilterChain class
        filter.doFilter(request, response, new FilterChain());
    }

    public class FilterChain extends MockFilterChain {
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            String someUrl = "/foo?bar=1";
            assertEquals(someUrl, ((HttpServletResponse) response).encodeURL(someUrl));
            assertEquals(someUrl, ((HttpServletResponse) response).encodeUrl(someUrl));
            assertEquals(someUrl, ((HttpServletResponse) response).encodeRedirectURL(someUrl));
            assertEquals(someUrl, ((HttpServletResponse) response).encodeRedirectUrl(someUrl));
            assertEquals("gs.web.util.DisableUrlSessionFilter$1", response.getClass().getName());
        }
    }

}
