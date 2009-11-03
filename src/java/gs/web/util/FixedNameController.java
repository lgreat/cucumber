package gs.web.util;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import gs.data.school.SchoolType;
import gs.data.school.Grades;
import gs.data.state.State;
import gs.data.util.ImportHelper;
import gs.data.util.Address;


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

    final gs.data.util.Formatter formatter = new gs.data.util.Formatter();

    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");
        String name = request.getParameter("name");
        String street = request.getParameter("street");
        String city = request.getParameter("city");
        String stateName = request.getParameter("state");
        State state = State.fromString(stateName);
        String zipcode = request.getParameter("zipcode");
        String phone = formatter.formatPhoneNumber(request.getParameter("phone"));
        String level = new Grades(request.getParameter("level")).getCommaSeparatedString();

        try {
            String newSchoolName = getImportHelper().fixSchoolName(name, State.CA, SchoolType.CHARTER);
            Address address = new Address(street,city,state,zipcode);
            getImportHelper().fixAddress(address,state);
            if (newSchoolName != null) {
                PrintWriter out = response.getWriter();
                out.print(
                                     "name:|:" + newSchoolName
                                + ":|:street:|:" + address.getStreet()
                                + ":|:city:|:" + address.getCity()
                                + ":|:state:|:" + address.getState()
                                + ":|:zipcode:|:" + address.getZip()
                                + ":|:phone:|:" + phone
                                + ":|:level:|:" + level
                );
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


