package gs.web.util;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
//import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import gs.data.school.LevelCode;
import gs.data.school.Grades;

/**
 * This controller provides a web interface to gs.data.school.LevelCode.createLevelCode();
 *
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public class GradeToLevelCodeController implements Controller {
    //private static final Logger _log = Logger.getLogger(GradeToLevelCodeController.class);

    /** Spring BEAN id */
    public static final String BEAN_ID = "gradeToLevelCodeController";

    /**
     * @see gs.data.school.LevelCode
     */
    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");
        String grades_param = request.getParameter("grades");
        String name_param = request.getParameter("name");

        //_log.warn("grades: '" + grades_param + "', name: '" + name_param + "'");
        
        try {
            Grades grades = new Grades(grades_param);

            LevelCode levelCode = LevelCode.createLevelCode(grades, name_param);
            if (levelCode != null) {
                PrintWriter out = response.getWriter();
                out.print(levelCode.toString());
                out.flush();
            }
        } catch (IllegalArgumentException e) {
            // do nothing, page will be blank
        }
        
        return null;
    }
}