package gs.web.widget;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.web.util.validator.EmailValidator;
import gs.web.util.ReadWriteController;
import gs.web.util.UrlBuilder;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.User;
import gs.data.community.IUserDao;
import gs.data.geo.City;
import gs.data.state.State;
import gs.data.admin.cobrand.ICobrandDao;
import gs.data.admin.cobrand.Cobrand;

import java.io.*;
import java.util.regex.Matcher;
import java.util.Date;
import java.util.Random;
import java.text.SimpleDateFormat;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class CustomizeSchoolSearchWidgetController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/schoolfinder/widget/customize.page";
    private static final Logger _log = Logger.getLogger(CustomizeSchoolSearchWidgetController.class);

    public static final int MINIMUM_WIDTH = 300;
    public static final int MINIMUM_HEIGHT = 434;
    public static final String DEFAULT_COBRAND = "www.greatschools.org";

    private IUserDao _userDao;
    private ICobrandDao _cobrandDao;
    private SchoolFinderWidgetEmail _schoolFinderWidgetEmail;
    private SchoolSearchWidgetController _schoolSearchWidgetController;

    protected void onBindAndValidate(HttpServletRequest request, Object commandObj,
                                     BindException errors) throws Exception {
        CustomizeSchoolSearchWidgetCommand command = (CustomizeSchoolSearchWidgetCommand) commandObj;
        String checkAjaxCall = request.getParameter("checkAjaxCall");

        if (StringUtils.isNotBlank(command.getEmail())) {
            EmailValidator emailValidator = new EmailValidator();
            if (emailValidator.supports(CustomizeSchoolSearchWidgetCommand.class)) {
                emailValidator.validate(command, errors);
            }
        }

        if (command.getWidth() < MINIMUM_WIDTH) {
            errors.rejectValue("width", null, "Minimum width is " + MINIMUM_WIDTH + ".");
        }
        if (command.getHeight() < MINIMUM_HEIGHT) {
            errors.rejectValue("height", null, "Minimum height is " + MINIMUM_HEIGHT + ".");
        }

        String cobrandName = command.getCobrandSite();
        if (cobrandName != null) {
            cobrandName.trim();
            // remove any prepended http://
            cobrandName = cobrandName.replaceAll("^http://", "");
            // remove any trailing slashes
            cobrandName = cobrandName.replaceAll("/+$","");
            // if no dot (.) is in the cobrand, append .greatschools.org
            if (!cobrandName.contains(".")) {
                cobrandName = cobrandName + ".greatschools.org";
            }

            // look for the cobrand; if not found, use www.greatschools.org
            Cobrand cobrand = _cobrandDao.getCobrandByHostname(cobrandName);
            if (cobrand == null) {
                command.setCobrandSite(DEFAULT_COBRAND);
            } else {
                command.setCobrandSite(cobrandName);
            }
        }

        //if its an ajax call for the widget then the parseSearchquery happens when the iframe is rendered we so dont need the below call.
        if(StringUtils.isBlank(checkAjaxCall)){
            SchoolSearchWidgetCommand widgetCommand = new SchoolSearchWidgetCommand();
            BindException widgetErrors = new BindException(widgetCommand, "widgetCommand");

            widgetCommand.setLat(command.getLat());
            widgetCommand.setLon(command.getLon());
            widgetCommand.setCityName(command.getCityName());
            widgetCommand.setState(command.getState());
            widgetCommand.setNormalizedAddress(command.getNormalizedAddress());

            _schoolSearchWidgetController.parseSearchQuery(
                command.getSearchQuery(),
                widgetCommand, request, widgetErrors);
            command.setCity(widgetCommand.getCity());
       }

        if ("yes".equals(command.getWidgetCodeCheck())) {
            if (!command.isTerms()) {
                errors.rejectValue("terms", null, "You must agree to the GreatSchools Terms of Use.");
            }
            if (StringUtils.isBlank(command.getEmail())) {
                errors.rejectValue("email", null, "You must enter an email address.");
            }

            if (!errors.hasErrors()) {
                String email = command.getEmail();
                User user = getUserDao().findUserFromEmailIfExists(email);
                if (user == null) {
                    // create new user
                    user = new User();
                    user.setEmail(email);
                    user.setHow("school_finder_widget");
                    getUserDao().saveUser(user);
                }

                // set widget unique id
                Random rand = new Random(System.currentTimeMillis());
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
                command.setUniqueId(user.getId() + "." + dateFormatter.format(new Date()) + "." + rand.nextInt(1000000));

                String widgetCode = getWidgetCode(command, request);
                sendWidgetCodeEmail(user, widgetCode, request);
                command.setWidgetCode(StringEscapeUtils.escapeHtml(widgetCode));
            }
        }

        // always reject for now
        errors.reject(null, "Show results");
    }

    //  This implementation of procesFormSubmission is written just to handle successfull submissions.
    // If an Ajax call is made,the iframe's URL is returned to the call.
    protected ModelAndView processFormSubmission(HttpServletRequest request,
                                             HttpServletResponse response,
                                             Object cmd,
                                             BindException errors)
                                      throws Exception {
        String checkAjaxCall = request.getParameter("checkAjaxCall");
        if(StringUtils.isNotBlank(checkAjaxCall)){
            CustomizeSchoolSearchWidgetCommand command = (CustomizeSchoolSearchWidgetCommand) cmd;
            String iframeUrl = command.getIframeUrl(request);
            PrintWriter out =response.getWriter();
            out.write(iframeUrl);
            return null;
        }else{
            return super.processFormSubmission(request,response,cmd,errors);
        }

    }
    protected void sendWidgetCodeEmail(User user, String widgetCode, HttpServletRequest request) {
        try {
            _schoolFinderWidgetEmail.sendToUser(user, widgetCode, request);
        } catch (Exception ex) {
            _log.error("Error sending schoolfinder widget code email to " + user, ex);
        }
    }

    protected String getWidgetCode(CustomizeSchoolSearchWidgetCommand command, HttpServletRequest request) {
        Resource resource = new ClassPathResource("/gs/web/widget/schoolFinderWidgetCode.txt");
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;
        UrlBuilder urlBuilder;
        String text = "Error creating widget code. Please try again or contact us at widget@greatschools.org";
        try {
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line.trim()).append(" ");
                line = reader.readLine();
            }
            text = buffer.toString();

            text = replaceText(text, "BG_COLOR", command.getBackgroundColor());
            text = replaceText(text, "BORDER_COLOR", command.getBordersColor());
            text = replaceText(text, "TEXT_COLOR", command.getTextColor());
            text = replaceText(text, "WIDGET_WIDTH", String.valueOf(command.getIframeWidth()+2));
            text = replaceText(text, "IFRAME_URL", command.getIframeUrl(request));
            text = replaceText(text, "IFRAME_WIDTH", String.valueOf(command.getIframeWidth()));
            text = replaceText(text, "IFRAME_HEIGHT", String.valueOf(command.getIframeHeight()));
            text = replaceText(text, "UNIQUE_ID", String.valueOf(command.getUniqueId()));
            
            PageHelper pageHelper = new PageHelper(SessionContextUtil.getSessionContext(request), request);
            String omniture_js = "http://www.greatschools.org/res/js/s_code.js";
            if (pageHelper.isDevEnvironment()) {
                omniture_js = "http://staging.greatschools.org/res/js/s_code_dev.js";
            }
            text = replaceText(text, "OMNITURE_JS", omniture_js);

            String externalTrackingJS = "http://www.greatschools.org/res/js/externalTracking.js";
            if (pageHelper.isDevEnvironment()) {
                externalTrackingJS = "http://staging.greatschools.org/res/js/externalTracking.js";
            }
            text = replaceText(text, "EXTERNAL_TRACKING_JS", externalTrackingJS);

            if (command.getCity() != null) {
                City city = command.getCity();
                urlBuilder = new UrlBuilder(city, UrlBuilder.CITY_PAGE);
                text = replaceText(text, "CITY_URL", "http://www.greatschools.org" + urlBuilder.asSiteRelative(request) + "?s_cid=wsbay93");

                if (city.getName().equals("New York") && State.NY.equals(city.getState())) {
                    text = replaceText(text, "CITY_SCHOOLS_LINK_TEXT", city.getName() + " City schools");
                } else if (city.getName().equals("Washington") && State.DC.equals(city.getState())) {
                    text = replaceText(text, "CITY_SCHOOLS_LINK_TEXT", city.getName() + ", DC schools");
                } else {
                    text = replaceText(text, "CITY_SCHOOLS_LINK_TEXT", city.getName() + " schools");
                }

                urlBuilder = new UrlBuilder(UrlBuilder.RESEARCH, city.getState(), null);
                urlBuilder.addParameter("s_cid", "wsbay93");
                text = replaceText(text, "STATE_URL", "http://www.greatschools.org" + urlBuilder.asSiteRelative(request));

                if (city.getName().equals("New York") && State.NY.equals(city.getState())) {
                    text = replaceText(text, "STATE_SCHOOLS_LINK_TEXT", city.getState().getLongName() + " State schools");
                } else if (city.getName().equals("Washington") && State.DC.equals(city.getState())) {
                    text = replaceText(text, "STATE_SCHOOLS_LINK_TEXT", "");
                } else {
                    text = replaceText(text, "STATE_SCHOOLS_LINK_TEXT", city.getState().getLongName() + " schools");
                }
            } else {
                text = replaceText(text, "CITY_URL", "http://www.greatschools.org/city/Fremont/CA?s_cid=wsbay93");
                text = replaceText(text, "CITY_SCHOOLS_LINK_TEXT", "Fremont schools");
                text = replaceText(text, "STATE_URL", "http://www.greatschools.org/modperl/go/CA?s_cid=wsbay93");
                text = replaceText(text, "STATE_SCHOOLS_LINK_TEXT", "California schools");
            }

            urlBuilder = new UrlBuilder(UrlBuilder.SCHOOL_FINDER_CUSTOMIZATION);
            urlBuilder.addParameter("s_cid", "wsbay93");
            text = replaceText(text, "WIDGET_CUSTOMIZATION_PAGE", urlBuilder.asFullUrl(command.getCobrandSite(),80));
        } catch (IOException e) {
            _log.error(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    _log.error(e);
                }
            }
        }
        return text;
    }

    protected String replaceText(String text, String key, String value) {
        return text.replaceAll("\\$" + key, Matcher.quoteReplacement(value)); // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6325587
    }

    public ICobrandDao getCobrandDao() {
        return _cobrandDao;
    }

    public void setCobrandDao(ICobrandDao cobrandDao) {
        _cobrandDao = cobrandDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public SchoolFinderWidgetEmail getSchoolFinderWidgetEmail() {
        return _schoolFinderWidgetEmail;
    }

    public void setSchoolFinderWidgetEmail(SchoolFinderWidgetEmail schoolFinderWidgetEmail) {
        _schoolFinderWidgetEmail = schoolFinderWidgetEmail;
    }

    public SchoolSearchWidgetController getSchoolSearchWidgetController() {
        return _schoolSearchWidgetController;
    }

    public void setSchoolSearchWidgetController(SchoolSearchWidgetController schoolSearchWidgetController) {
        _schoolSearchWidgetController = schoolSearchWidgetController;
    }
}