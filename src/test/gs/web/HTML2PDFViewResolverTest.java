package gs.web;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.createStrictMock;
import static org.easymock.classextension.EasyMock.verify;

public class HTML2PDFViewResolverTest extends BaseControllerTestCase {

    HTML2PDFViewResolver _html2PDFViewResolver;

    ViewResolver _viewResolver = createStrictMock(ViewResolver.class);

    View testView = new View() {
        public String getContentType() {
            return "text/html";
        }

        public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
            response.setContentType("text/html");
            response.getWriter().write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
            response.getWriter().write("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">");
            response.getWriter().write("<head><title>Test html page</title></head>");
            response.getWriter().write("<body>");
            response.getWriter().write("<h1>Test</h1>");
            response.getWriter().write("</body></html>");
            response.getWriter().flush();
        }
    };

    @Before
    public void setUp() throws Exception {
        super.setUp();

        _html2PDFViewResolver = new HTML2PDFViewResolver();
        _html2PDFViewResolver.setViewResolver(_viewResolver);
    }

    @Test
    public void testPdfGenerationWorks() throws Exception {
        String viewName = "testHTML2PDFViewResolverView";
        Locale local = Locale.US;

        expect(_viewResolver.resolveViewName(eq(viewName), eq(local))).andReturn(testView);

        Map<String,Object> model = new HashMap<String,Object>();

        replay(_viewResolver);
        View pdfView = _html2PDFViewResolver.resolveViewName("testHTML2PDFViewResolverView", Locale.US);
        pdfView.render(model, getRequest(), getResponse());

        byte[] bytes = getResponse().getContentAsByteArray();
        assertTrue(bytes.length > 0);
        verify(_viewResolver);
    }
}