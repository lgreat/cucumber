package gs.web.school;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.web.request.RequestInfo;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;

public class PerlFetchController extends AbstractSchoolController {
    private static final Logger _log = Logger.getLogger(PerlFetchController.class);

    protected static final String VIEW_NOT_FOUND = "/status/error404";
    protected static final String VIEW_ERROR = "/status/error500";
    protected static final String DEV_HOST = "dev.greatschools.org";
    public static final String HTML_ATTRIBUTE = "perlHtml";

    private SchoolProfileHeaderHelper _schoolProfileHeaderHelper;
    private String _perlContentPath;
    private String _viewName;

    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HashMap<String, Object> model = new HashMap<String, Object>();

        School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);

        // Preschool profile pages should be hosted from pk.greatschools.org (GS-12127). Redirect if needed
        ModelAndView preschoolRedirectMandV = getPreschoolRedirectViewIfNeeded(request, school);
        if (preschoolRedirectMandV != null) {
            return preschoolRedirectMandV;
        }

        if (school == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView(VIEW_NOT_FOUND);
        }

        String perlResponse;
        String href = getAbsoluteHref(school, request);
        String view = getViewName();

        try {
            _schoolProfileHeaderHelper.updateModel(request, response, school, model);

            perlResponse = getResponseFromUrl(href);

            if (StringUtils.isNotBlank(perlResponse)) {
                model.put(HTML_ATTRIBUTE, perlResponse);
                if (!shouldIndex(school, perlResponse)) {
                    model.put("noIndexFlag", true);
                }
            } else {
                _log.error("Empty response received from perl at " + href);
                response.sendError(500);
                view = VIEW_ERROR;
            }
        } catch (BadResponseException e) {
            _log.error("Problem retrieving data from " + href + ". Aborting and bubbling up response code ", e);
            response.sendError(e.getResponseCode(), null);

            if (e.getResponseCode() == 404 ) {
                view = VIEW_NOT_FOUND;
            } else if (e.getResponseCode() == 500 ) {
                model.put("javax.servlet.error.exception", e);
                view = VIEW_ERROR;
            } else {
                model.put("javax.servlet.error.exception", e);
                view = VIEW_ERROR;
            }
        }

        return new ModelAndView(view, model);
    }


    /**
     * Determine if data from this controller should be indexed by crawlers or not.  Implemented for GS-11686.
     * @param school School object for this page
     * @param perlResponse The perl response used in the body of the page
     * @return true if the page should be indexed.  false otherwise.
     */
    protected boolean shouldIndex(School school, String perlResponse) {
        // Always index by default, subclasses can override.
        return true;
    }

    protected String getAbsoluteHref(School school, HttpServletRequest request) {
        String relativePath = getPerlContentPath();

        relativePath = relativePath.replaceAll("\\$STATE", school.getDatabaseState().getAbbreviationLowerCase());
        relativePath = relativePath.replaceAll("\\$ID", String.valueOf(school.getId()));

        String href = request.getScheme() + "://localhost" +
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

    protected String getResponseFromUrl(String absoluteHref) throws BadResponseException, IOException {
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
        } finally {
            _log.info("Fetching from " + absoluteHref + " took " + (System.currentTimeMillis() - startTime)
                    + " milliseconds");
            if (connection != null) {
                connection.disconnect();
            }
        }

        return response.toString();
    }

    /**
     * Check a school's levelcode and type to see if a redirect is needed. GS-12127.
     * Not implemented by default. Only TeachersStudentsController handles Preschools
     *
     * @param request
     * @param school
     * @return A ModelAndView that includes a redirect view, otherwise null
     */
    public ModelAndView getPreschoolRedirectViewIfNeeded(HttpServletRequest request, School school) {
       return null;
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
