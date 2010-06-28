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

public class PerlFetchController extends AbstractSchoolController implements Controller {
    private static final Logger _log = Logger.getLogger(PerlFetchController.class);

    protected static final String VIEW_NOT_FOUND = "/status/error404";
    protected static final String VIEW_ERROR = "/status/error500";
    protected static final String DEV_HOST = "profile.dev.greatschools.org";
    public static final String HTML_ATTRIBUTE = "perlHtml";

    private SchoolProfileHeaderHelper _schoolProfileHeaderHelper;
    private String _perlContentPath;
    private String _viewName;

    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HashMap<String, Object> model = new HashMap<String, Object>();

        School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);

        if (school == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView(VIEW_NOT_FOUND);
        }
        String perlResponse;
        String href = getAbsoluteHref(school, request);
        String view = getViewName();

        try {
            perlResponse = getResponseFromUrl(href);

            _schoolProfileHeaderHelper.updateModel(school, model);

            model.put(HTML_ATTRIBUTE, perlResponse);
        } catch (BadResponseException e) {
            _log.error("Problem retrieving data from " + href + ". Aborting and bubbling up response code ", e);
            response.sendError(e.getResponseCode(), null);

            if (e.getResponseCode() == 404 ) {
                view = VIEW_NOT_FOUND;
            } else if (e.getResponseCode() == 500 ) {
                model.put("javax.servlet.error.exception", e);
                view = VIEW_ERROR;
            }
        }

        return new ModelAndView(view, model);
    }

    protected String getAbsoluteHref(School school, HttpServletRequest request) {
        String relativePath = getPerlContentPath();

        relativePath = relativePath.replaceAll("\\$STATE", school.getDatabaseState().getAbbreviationLowerCase());
        relativePath = relativePath.replaceAll("\\$ID", String.valueOf(school.getId()));

        String href = request.getScheme() + "://" + request.getServerName() +
                ((request.getServerPort() != 80)?(":" + request.getServerPort()):"") +
                relativePath;

        if (UrlUtil.isDeveloperWorkstation(request.getServerName())) {
            href = "http://" + DEV_HOST + relativePath;
        }

        return href;
    }

    class BadResponseException extends Exception {
        private int _responseCode;

        public BadResponseException(int responseCode, String url) {
            super("Request to " + url + " returned bad response code: " + responseCode);
            _responseCode = responseCode;
        }

        public int getResponseCode() {
            return _responseCode;
        }
    }

    protected String getResponseFromUrl(String absoluteHref) throws BadResponseException {
        HttpURLConnection connection = null;
        BufferedReader reader;
        StringBuilder response = new StringBuilder();
        String line;
        URL serverAddress;

        long startTime = System.currentTimeMillis();
        try {
            serverAddress = new URL(absoluteHref);

            connection = (HttpURLConnection)serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(10000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode >= 400 ) {
                throw new BadResponseException(responseCode, absoluteHref);
            }

            reader  = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while ((line = reader.readLine()) != null) {
                response.append(line).append('\n');
            }

            _log.info("Got response code " + responseCode);
        } catch (MalformedURLException e) {
            _log.error("Could not understand given url: " + absoluteHref, e);
        } catch (ProtocolException e) {
            _log.error("Error reading from given url: " + absoluteHref, e);
        } catch (IOException e) {
            _log.error("Error reading from given url: " + absoluteHref, e);
        }
        finally {
            _log.info("Fetching from " + absoluteHref + " took " + (System.currentTimeMillis() - startTime)
                    + " milliseconds");
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
