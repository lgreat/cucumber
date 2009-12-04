package gs.web.community.registration;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.PrintWriter;

import gs.data.geo.*;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.community.IUserDao;
import gs.web.util.validator.UserCommandValidator;

/**
 * @author greatschools.org>
 */
public class RegistrationAjaxController implements Controller {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String BEAN_ID = "/community/registrationAjax.page";

    private IGeoDao _geoDao;
    private IUserDao _userDao;
    private StateManager _stateManager;

    final public static String TYPE_PARAM = "type";
    final public static String EMAIL_PARAM = "email";
    final public static String USER_NAME_PARAM = "un";
    final public static String FIRST_NAME_PARAM = "fn";
    final public static String CITY_TYPE = "city";
    final public static String COUNTY_TYPE = "county";
    final public static String CBI_CALL = "cbicall";

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PrintWriter out = response.getWriter();
        String type = request.getParameter(TYPE_PARAM);
        if (type != null) {
            if (CITY_TYPE.equals(type)) {
                outputCitySelect(request, out);
            } else if (COUNTY_TYPE.equals(type)) {
                outputCountySelect(request,out);
            }
            return null;
        }

        if (request.getParameter(EMAIL_PARAM) != null) {
            validateEmail(request, out);
            return null;
        }

        if (request.getParameter(USER_NAME_PARAM) != null) {
            validateUsername(request, out);
            return null;
        }

        if (request.getParameter(FIRST_NAME_PARAM) != null) {
            validateFirstName(request, out);
            return null;
        }

        return null;
    }

    protected void validateEmail(HttpServletRequest request, PrintWriter out) {
        String email = request.getParameter(EMAIL_PARAM);
        org.apache.commons.validator.EmailValidator emv = org.apache.commons.validator.EmailValidator.getInstance();

        //TODO: Check if email is in use by a full fledged member?
        if (!emv.isValid(email)) {
            out.print("invalid");
        } else {
            out.print("valid");
        }
    }

    protected void validateUsername(HttpServletRequest request, PrintWriter out) {
        String username = request.getParameter(USER_NAME_PARAM);
        boolean cbcall = false;
        if(request.getParameter(CBI_CALL) != null){
            cbcall = true;
        }
         UserCommandValidator validator = new UserCommandValidator();
        if (username.length() < UserCommandValidator.SCREEN_NAME_MINIMUM_LENGTH || username.length() > UserCommandValidator.SCREEN_NAME_MAXIMUM_LENGTH ) {
           out.print("invalid");
        }else if(validator.screenNameHasInvalidCharacters(username,cbcall)){
             out.print("invalidchars");
        } else if (_userDao.findUserFromScreenNameIfExists(username) != null) {
            out.print("inuse");
        } else {
            out.print("valid");
        }
    }

    protected void validateFirstName(HttpServletRequest request, PrintWriter out) {
        String firstName = request.getParameter(FIRST_NAME_PARAM);

        if (StringUtils.isEmpty(firstName) ||
                firstName.length() > UserCommandValidator.FIRST_NAME_MAXIMUM_LENGTH ||
                firstName.length() < UserCommandValidator.FIRST_NAME_MINIMUM_LENGTH) {
            out.print("invalid_length");
        } else if (!StringUtils.containsNone(firstName, UserCommandValidator.FIRST_NAME_DISALLOWED_CHARACTERS)) {
            out.print("invalid_chars");
        } else {
            out.print("valid");
        }
    }

    protected void outputCitySelect(HttpServletRequest request, PrintWriter out) {
        State state = _stateManager.getState(request.getParameter("state"));
        List<City> cities = _geoDao.findCitiesByState(state);
        String onChange = request.getParameter("onchange");
        String citySelectName =
            (request.getParameter("citySelectName") != null ? request.getParameter("citySelectName") : "city");
        String citySelectId =
                (request.getParameter("citySelectId") != null ? request.getParameter("citySelectId") : "citySelect");

        if (request.getParameter("showNotListed") != null && Boolean.valueOf(request.getParameter("showNotListed"))) {
            City notListed = new City();
            notListed.setName("My city is not listed");
            cities.add(notListed);
        }

        if (cities.size() > 0) {
            out.print("<select id=\"" + citySelectId + "\" name=\"" + citySelectName + "\" class=\"selectCity\" tabindex=\"10\"" +
                 (StringUtils.isNotBlank(onChange) ? " onchange=\"" + onChange + "\"" : "") +  ">");
            outputOption(out, "", "- Choose city -", true);
            for (City city : cities) {
                outputOption(out, city.getName(), city.getName());
            }
            out.print("</select>");
        }
    }

    protected void outputCountySelect(HttpServletRequest request, PrintWriter out) {
        State state = _stateManager.getState(request.getParameter("state"));
        List<ICounty> counties = _geoDao.findCounties(state);
        if (counties.size() > 0) {
            out.print("<select id=\"countySelect\" name=\"county\" class=\"selectCounty\">");
            outputOption(out, "", "Choose county", true);
            for (ICounty county : counties) {
                outputOption(out, county.getName(), county.getName());
            }
            out.print("</select>");
        }
    }

    protected void outputOption(PrintWriter out, String value, String name) {
        outputOption(out, value, name, false);
    }

    protected void outputOption(PrintWriter out, String value, String name, boolean selected) {
        out.print("<option ");
        if (selected) {
            out.print("selected=\"selected\" ");
        }
        out.print("value=\"" + value + "\">");
        out.print(StringEscapeUtils.escapeHtml(name));
        out.print("</option>");
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
