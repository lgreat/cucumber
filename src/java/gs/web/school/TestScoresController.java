package gs.web.school;

import gs.data.school.School;
import gs.web.util.UrlUtil;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;

public class TestScoresController extends AbstractSchoolController implements Controller {
    private static Logger _log = Logger.getLogger(TestScoresController.class);

    private SchoolProfileHeaderHelper _schoolProfileHeaderHelper;

    private String _perlContentPath;

    private String _viewName;
  
    public static final String HTML_ATTRIBUTE = "testScoresHtml";

    public static final String DEV_HOST = "ssprouse.dev.greatschools.org";

    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        HashMap<String, Object> model = new HashMap<String, Object>();

        School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);

        String perlResponse;

        String href = getAbsoluteHref(request);

        String view = getViewName();

        try {
            perlResponse = getResponseFromUrl(href);

            if (school != null) {
                _schoolProfileHeaderHelper.updateModel(school, model);
            }

            model.put("testScoresHtml", perlResponse);
        } catch (BadResponseException e) {
            _log.error("Problem retrieving data from " + href + ". Aborting and bubbling up response code ", e);
            response.sendError(e.getResponseCode(), null);

            if (e.getResponseCode() == 404 ) {
                view = "/status/error404";
            } else if (e.getResponseCode() == 500 ) {
                model.put("javax.servlet.error.exception", e);
                view = "/status/error500";
            }
        }

        return new ModelAndView(view, model);
    }
    
    public String getAbsoluteHref(HttpServletRequest request) {
        String relativePath = getPerlContentPath();

        String href = request.getProtocol() + "://" + request.getServerName() + ":" + request.getServerPort() + relativePath;

        if (UrlUtil.isDeveloperWorkstation(request.getServerName())) {
            href = "http://" + DEV_HOST + relativePath;
        }
        
        return href;
    }

    class BadResponseException extends Exception {
        private int _responseCode;
        
        public BadResponseException(int responseCode) {
            super("Request returned bad response code: " + responseCode);
            _responseCode = responseCode;
        }

        public int getResponseCode() {
            return _responseCode;
        }
    }
    
    public String getResponseFromUrl(String absoluteHref) throws BadResponseException {

        HttpURLConnection connection = null;
        BufferedReader reader;
        StringBuilder response = new StringBuilder();
        String line;
        URL serverAddress;

        try {
            serverAddress = new URL(absoluteHref);

            connection = (HttpURLConnection)serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(1000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode >= 400 ) {
                throw new BadResponseException(responseCode);
            }

            reader  = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while ((line = reader.readLine()) != null) {
                response.append(line).append('\n');
            }

        } catch (MalformedURLException e) {
            _log.debug("Could not understand given url: " + absoluteHref, e);
        } catch (ProtocolException e) {
            _log.debug("Error reading from given url: " + absoluteHref, e);
        } catch (IOException e) {
            _log.debug("Error reading from given url: " + absoluteHref, e);
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return response.toString();
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public String getPerlContentPath() {
        return _perlContentPath;
    }

    public void setPerlContentPath(String perlContentPath) {
        _perlContentPath = perlContentPath;
    }

    public SchoolProfileHeaderHelper getSchoolProfileHeaderHelper() {
        return _schoolProfileHeaderHelper;
    }

    public void setSchoolProfileHeaderHelper(SchoolProfileHeaderHelper schoolProfileHeaderHelper) {
        _schoolProfileHeaderHelper = schoolProfileHeaderHelper;
    }
}
