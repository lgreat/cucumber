package gs.web.util;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author thuss
 */
public class MockFilterChain implements FilterChain {

    private ServletException _throwSe = null;

    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (_throwSe != null) {
            throw _throwSe;
        }
    }

    // Tell the chain to throw an exception
    public void setException(ServletException e) {
        _throwSe = e;
    }
}
