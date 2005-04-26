package gs.web;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * This is a sample servlet, typically you would not use this, but it is useful
 * for testing the sanity of your web application configuration.
 *
 * @author <a href="trajano@yahoo.com">Archimedes Trajano</a>
 * @version $Id: HelloWorldServlet.java,v 1.3 2005/04/26 21:24:12 apeterson Exp $
 * @web.servlet name="HelloWorld"
 * @web.servlet-mapping url-pattern="/HelloWorld"
 */
public class HelloWorldServlet extends HttpServlet {
    /**
     * This prints out the standard "Hello world" message with a date stamp.
     *
     * @param request  the HTTP request object
     * @param response the HTTP response object
     * @throws IOException thrown when there is a problem getting the writer
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        response.getWriter().println("Hello world on " + new Date());
    }
}
