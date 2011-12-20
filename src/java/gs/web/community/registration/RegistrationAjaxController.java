package gs.web.community.registration;

import gs.data.json.JSONException;
import gs.data.json.JSONObject;
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
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class RegistrationAjaxController implements Controller {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String BEAN_ID = "/community/registrationAjax.page";

    private IGeoDao _geoDao;
    private IUserDao _userDao;
    private StateManager _stateManager;

    final public static String FORMAT_PARAM = "format";
    final public static String JSONP_CALLBACK_PARAM = "jsoncallback";
    final public static String TYPE_PARAM = "type";
    final public static String EMAIL_PARAM = "email";
    final public static String USER_NAME_PARAM = "un";
    final public static String USER_NAME_SUGGESTIONS = "suggest_un";
    final public static String FIRST_NAME_PARAM = "fn";
    final public static String LAST_NAME_PARAM = "ln";
    final public static String CITY_TYPE = "city";
    final public static String CITY_OPTIONS_TYPE = "cityOptions";
    final public static String COUNTY_TYPE = "county";
    final public static String CBI_CALL = "cbicall";

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PrintWriter out = response.getWriter();
        String type = request.getParameter(TYPE_PARAM);
         if (request.getParameter(FIRST_NAME_PARAM) != null
                 && StringUtils.equals("true", request.getParameter(USER_NAME_SUGGESTIONS))
                 && StringUtils.equals("json", request.getParameter(FORMAT_PARAM))) {
                suggestUsername(request,out);
                return null;
         }

        if (StringUtils.equals("json", request.getParameter(FORMAT_PARAM))) {
            if (StringUtils.equals(CITY_TYPE, type)) {
                outputCityJson(request, out);
            }
            return null;
        }
        if (type != null) {
            if (CITY_TYPE.equals(type)) {
                outputCitySelect(request, out);
            } else if (COUNTY_TYPE.equals(type)) {
                outputCountySelect(request,out);
            } else if (CITY_OPTIONS_TYPE.equals(type)) {
                outputCityOptions(request, out);
            }
            return null;
        }

        if (request.getParameter(EMAIL_PARAM) != null) {
            validateEmail(request, response, out);
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

    protected void validateEmail(HttpServletRequest request,HttpServletResponse response, PrintWriter out) {
        String email = request.getParameter(EMAIL_PARAM);
        org.apache.commons.validator.EmailValidator emv = org.apache.commons.validator.EmailValidator.getInstance();

        response.setContentType("text/plain");

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

    protected void suggestUsername (HttpServletRequest request, PrintWriter out) {
        String firstName = request.getParameter(FIRST_NAME_PARAM);
        String lastName = request.getParameter(LAST_NAME_PARAM);
        boolean cbcall = false;
        if(request.getParameter(CBI_CALL) != null){
            cbcall = true;
        }
        JSONObject rval = new JSONObject();
        String username;
        String suggestion="";
        String usernames="";
        if(StringUtils.isNotBlank(firstName)){
            username = firstName;
            if(StringUtils.isNotBlank(lastName)){
                username += lastName.substring(0,1);
            }
            for(int i=0;i<3;i++){
                if(i==0){
                    usernames += constructUsername(username,false) + ",";
                }else{
                    usernames += constructUsername(username,true) + ",";
                }
            }
            usernames = usernames.substring(0,usernames.length()-1);
            suggestion = validateUsernames(usernames,cbcall);
          }
        try{
            rval.put("suggestion", suggestion);
        }catch(JSONException jsone){
            _log.error("Error converting username suggestion to JSON: " + jsone, jsone);
        }
        String jsonCallbackParam = getSanitizedJsonpParam(request);
        String res= jsonCallbackParam+"("+rval+");";
        out.print(res);
    }

    protected String constructUsername(String name,boolean addRandom){
       if(addRandom){
           Long random = Math.round(Math.random()*100);
           name = name + random;
       } 
       name = checkScreenameLength(name);
       return name;
    }

    protected String checkScreenameLength(String screenName) {
        int num = 1;
        if(screenName.length()<UserCommandValidator.SCREEN_NAME_MINIMUM_LENGTH){
          for(int i=screenName.length();i<UserCommandValidator.SCREEN_NAME_MINIMUM_LENGTH;i++){
            num = num *10;
          }
          int diff = UserCommandValidator.SCREEN_NAME_MINIMUM_LENGTH - screenName.length() ;
          long randomFiller = Math.round(Math.random()*num);
          String paddedRandom =String.format("%0"+diff+"d",randomFiller);
          screenName= screenName + paddedRandom;
        }else if(screenName.length()>UserCommandValidator.SCREEN_NAME_MAXIMUM_LENGTH){
          screenName = screenName.substring(0,12);
          long random = Math.round(Math.random()*100);
          screenName = screenName + random;
        }
        return screenName;
    }

    protected String validateUsernames(String uns,boolean cbcall) {
        String suggestion="";
        String names="";

        Map<String,String> validNames=new LinkedHashMap();
        if(StringUtils.isNotBlank(uns)){
            String[] usernames = uns.split(",");
            for(String username:usernames){
                String validation = usernameValidator(username,cbcall);
                if(validation.equals("valid")){
                    validNames.put(username.toLowerCase(),validation);
                    names = names+",'"+username.toLowerCase()+"'";
                }
            }
            if(StringUtils.isNotBlank(names) && validNames.size() >0){
                names = names.substring(1,names.length());
                _userDao.findUsersFromScreenNameIfExists(names,validNames);
                for(String key: validNames.keySet()){
                    if(validNames.get(key).equals("valid")){
                        suggestion = key;
                        break;
                    }
                }
            }
        }
        return suggestion;
    }

    protected String usernameValidator(String username,boolean cbcall){
        String output;
        UserCommandValidator validator = new UserCommandValidator();
        if (username.length() < UserCommandValidator.SCREEN_NAME_MINIMUM_LENGTH || username.length() > UserCommandValidator.SCREEN_NAME_MAXIMUM_LENGTH ) {
           output="invalid";
        }else if(validator.screenNameHasInvalidCharacters(username,cbcall)){
           output="invalidchars";
        }else {
           output="valid";
        }
        return output;
    }

    protected String getSanitizedJsonpParam(HttpServletRequest request) {
        String jsonpParam = request.getParameter(JSONP_CALLBACK_PARAM);
        if ( StringUtils.isEmpty(jsonpParam)) return null;
        if ( StringUtils.length(jsonpParam) > 128 ) return null;
        if ( !StringUtils.startsWithIgnoreCase(jsonpParam,"jsonp") && !StringUtils.startsWithIgnoreCase(jsonpParam,"jQuery")) return null;
        return jsonpParam;
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

    protected void outputCityJson(HttpServletRequest request, PrintWriter out) {
        JSONObject rval = new JSONObject();
        State state = _stateManager.getState(request.getParameter("state"));
        List<City> cities = _geoDao.findCitiesByState(state);
        try {
            List<JSONObject> cityList = new ArrayList<JSONObject>(cities.size());
            JSONObject chooseCity = new JSONObject();
            chooseCity.put("name", "- Choose city -");
            cityList.add(chooseCity);
            for (City city: cities) {
                JSONObject cityJson = new JSONObject();
                cityJson.put("name", city.getName());
                cityList.add(cityJson);
            }
            JSONObject notListed = new JSONObject();
            notListed.put("name", "My city is not listed");
            cityList.add(notListed);
            rval.put("cities", cityList);
        } catch (JSONException jsone) {
            _log.error("Error converting city list to JSON: " + jsone, jsone);
        }
        String jsonCallbackParam = getSanitizedJsonpParam(request);
        if(jsonCallbackParam != null){
            String res= jsonCallbackParam+"("+rval+");";
            out.print(res);
        }else{
            out.print(rval.toString());
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

    protected void outputCityOptions(HttpServletRequest request, PrintWriter out) {
        State state = _stateManager.getState(request.getParameter("state"));
        List<City> cities = _geoDao.findCitiesByState(state);

        if (request.getParameter("showNotListed") != null && Boolean.valueOf(request.getParameter("showNotListed"))) {
            City notListed = new City();
            notListed.setName("My city is not listed");
            cities.add(notListed);
        }

        if (cities.size() > 0) {
            outputOption(out, "", "- Choose city -", true);
            for (City city : cities) {
                outputOption(out, city.getName(), city.getName());
            }
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