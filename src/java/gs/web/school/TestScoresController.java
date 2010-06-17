package gs.web.school;

import gs.data.util.string.StringUtils;
import gs.web.util.UrlUtil;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;

public class TestScoresController implements Controller {
    private static Logger _log = Logger.getLogger(TestScoresController.class);

    private String _perlContentPath;

    private String _viewName;
  
    public static final String HTML_ATTRIBUTE = "testScoresHtml";

    public static final String DEV_HOST = "ssprouse.dev.greatschools.org";

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        HashMap<String, Object> model = new HashMap<String, Object>();

        String perlResponse = null;

        String href = getAbsoluteHref(request);

        try {
            perlResponse = getResponseFromUrl(href);
        } catch (BadResponseException e) {
            _log.debug("Problem retrieving data from perl. Aborting and bubbling up response code ", e);
            response.sendError(e.getResponseCode(), null);

            if (e.getResponseCode() == 404 ) {
                setViewName("/status/error404");
            } else if (e.getResponseCode() == 500 ) {
                setViewName("/status/error500");
            }
        }

        model.put("testScoresHtml", perlResponse);
        return new ModelAndView(this.getViewName(), model);
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

        public void setResponseCode(int responseCode) {
            this._responseCode = responseCode;
        }
    }
    
    public String getResponseFromUrl(String absoluteHref) throws BadResponseException {

        HttpURLConnection connection = null;
        BufferedReader reader  = null;
        StringBuilder response = new StringBuilder();
        String line = null;
        URL serverAddress = null;

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
}
