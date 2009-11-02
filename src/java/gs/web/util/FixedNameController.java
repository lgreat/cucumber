package gs.web.util;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import gs.data.school.SchoolType;
import gs.data.state.State;
import gs.data.util.ImportHelper;


/**
 * Created by IntelliJ IDEA.
 * User: eddie
 * Date: Oct 30, 2009
 * Time: 1:10:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class FixedNameController  implements Controller{
    /** Spring BEAN id */
    public static final String BEAN_ID = "fixedNameController";

    private ImportHelper _importHelper;

    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");
        String name = request.getParameter("name");

        try {
            String newSchoolName = getImportHelper().fixSchoolName(name, State.CA, SchoolType.CHARTER);

            if (newSchoolName != null) {
                PrintWriter out = response.getWriter();
                out.print(newSchoolName);
                out.flush();
            }
        } catch (IllegalArgumentException e) {
            // do nothing, page will be blank
        }

        return null;
    }

    public ImportHelper getImportHelper() {
        return _importHelper;
    }

    public void setImportHelper(ImportHelper importHelper) {
        _importHelper = importHelper;
    }

}


