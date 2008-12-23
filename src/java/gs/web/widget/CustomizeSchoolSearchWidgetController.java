package gs.web.widget;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.validation.BindException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;

import gs.web.util.validator.EmailValidator;
import gs.web.util.ReadWriteController;
import gs.web.util.UrlBuilder;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.User;
import gs.data.community.IUserDao;
import gs.data.geo.City;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.Date;
import java.util.Random;
import java.text.SimpleDateFormat;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CustomizeSchoolSearchWidgetController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/schoolfinder/widget/customize.page";
    private static final Logger _log = Logger.getLogger(CustomizeSchoolSearchWidgetController.class);

    public static final int MINIMUM_WIDTH = 300;
    public static final int MINIMUM_HEIGHT = 434;

    private IUserDao _userDao;
    private SchoolFinderWidgetEmail _schoolFinderWidgetEmail;
    private SchoolSearchWidgetController _schoolSearchWidgetController;

    protected void onBindAndValidate(HttpServletRequest request, Object commandObj,
                                     BindException errors) throws Exception {
        CustomizeSchoolSearchWidgetCommand command = (CustomizeSchoolSearchWidgetCommand) commandObj;

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

        SchoolSearchWidgetCommand widgetCommand = new SchoolSearchWidgetCommand();
        BindException widgetErrors = new BindException(widgetCommand, "widgetCommand");
        _schoolSearchWidgetController.parseSearchQuery(
                command.getSearchQuery(),
                _schoolSearchWidgetController.getGoogleApiKey(request.getServerName()),
                widgetCommand, request, widgetErrors);
        command.setCity(widgetCommand.getCity());

        if (request.getParameter("submit") != null || request.getParameter("submit.x") != null) {
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
        String text = "Error creating widget code. Please try again or contact us at widget@greatschools.net";
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
            String omniture_js = "http://www.greatschools.net/res/js/s_code.js";
            if (pageHelper.isDevEnvironment()) {
                omniture_js = "http://staging.greatschools.net/res/js/s_code_dev.js";
            }
            text = replaceText(text, "OMNITURE_JS", omniture_js);

            String externalTrackingJS = "http://www.greatschools.net/res/js/externalTracking.js";
            if (pageHelper.isDevEnvironment()) {
                externalTrackingJS = "http://staging.greatschools.net/res/js/externalTracking.js";
            }
            text = replaceText(text, "EXTERNAL_TRACKING_JS", externalTrackingJS);

            if (command.getCity() != null) {
                City city = command.getCity();
                urlBuilder = new UrlBuilder(city, UrlBuilder.CITY_PAGE);
                text = replaceText(text, "CITY_URL", "http://www.greatschools.net" + urlBuilder.asSiteRelative(request) + "?s_cid=wsbay93");
                text = replaceText(text, "CITY_NAME", city.getName());

                urlBuilder = new UrlBuilder(UrlBuilder.RESEARCH, city.getState(), null);
                urlBuilder.addParameter("s_cid", "wsbay93");
                text = replaceText(text, "STATE_URL", "http://www.greatschools.net" + urlBuilder.asSiteRelative(request));
                text = replaceText(text, "STATE_NAME", city.getState().getLongName());
            } else {
                text = replaceText(text, "CITY_URL", "http://www.greatschools.net/city/Fremont/CA?s_cid=wsbay93");
                text = replaceText(text, "CITY_NAME", "Fremont");
                text = replaceText(text, "STATE_URL", "http://www.greatschools.net/modperl/go/CA?s_cid=wsbay93");
                text = replaceText(text, "STATE_NAME", "California");
            }

            urlBuilder = new UrlBuilder(UrlBuilder.SCHOOL_FINDER_CUSTOMIZATION);
            urlBuilder.addParameter("s_cid", "wsbay93");
            text = replaceText(text, "WIDGET_CUSTOMIZATION_PAGE", urlBuilder.asFullUrl(request));
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
