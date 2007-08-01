package gs.web.school;

import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.apache.commons.lang.StringUtils;
import gs.data.school.School;
import gs.data.school.ISchoolDao;
import gs.data.state.State;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public abstract class SchoolCommandController extends AbstractCommandController {

    //private static final Logger _log = Logger.getLogger(SchoolCommandController.class);

    private ISchoolDao _schoolDao;

    /** The name of the error view - see pages-servlet.xml */
    private String _errorViewName = "/school/error";

    public ModelAndView handleRequestInternal (HttpServletRequest request, HttpServletResponse response)
			throws Exception {

        SchoolCommand schoolCommand = (SchoolCommand)getCommand(request);
        ServletRequestDataBinder binder = bindAndValidate(request, schoolCommand);
		BindException errors = new BindException(binder.getBindingResult());

        // reset the school for this request
        School _school;
        _school = null;

        // make sure we have a valid school
        if (!errors.hasErrors()) {
            State state = schoolCommand.getState();
            try {
                Integer id = new Integer(schoolCommand.getSchoolId());
                School s = _schoolDao.getSchoolById(state, id);
                if (s.isActive()) {
                    _school = s;
                } else {
                    errors.reject("school", "School is not active: " + s);
                }
            } catch (Exception e) {
                errors.reject("school", "Could not get a valid or active school");
            }
        }

        if (!errors.hasErrors()) {
            return handle(request, response, schoolCommand, errors);
        } else {
            // display error view.
            // Todo: make use of the error data in the BindException object
            return new ModelAndView(getErrorViewName());
        }
    }

    public School getSchool() {
        //return null;
        throw new RuntimeException ("not implemented yet");
    }

    /**
     * @return An ISchoolDao type
     */
    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    /**
     * Spring setter
     * @param schoolDao used to get school
     */
    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }


    public void setErrorViewName(String viewName) {
        if (StringUtils.isNotBlank(viewName)) {
            _errorViewName = viewName;
        } else {
            throw new IllegalArgumentException ("viewname cannot be blank");
        }
    }

    protected String getErrorViewName() {
        return _errorViewName;
    }
}
