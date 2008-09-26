package gs.web.path;

import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Sep 24, 2008
 * Time: 10:51:44 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IDirectoryStructureUrlController {
    final public static String FIELDS = "directoryStructureUrlFields";

    ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws java.lang.Exception;

    boolean shouldHandleRequest(DirectoryStructureUrlFields fields);
}
