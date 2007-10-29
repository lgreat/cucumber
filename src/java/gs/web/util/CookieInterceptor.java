package gs.web.util;

import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 * Interceptor to set http response headers
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class CookieInterceptor implements HandlerInterceptor {
    public static final int EXPIRE_AT_END_OF_SESSION = -1;
    public static final int EXPIRE_NOW = 0;
    public static final String AB_CONFIGURATION_FILE_CLASSPATH = "/gs/web/util/abConfig.txt";
    protected static int[] _abCutoffs = null;
    protected static int _cutoffTotal = 0;
    private static final Log _log = LogFactory.getLog(CookieInterceptor.class);

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        SessionContext sessionContext = (SessionContext) request.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);

        // We don't set cookies for cacheable pages
        if (!(o instanceof CacheablePageController)) {
            Cookie trno = buildTrnoCookie(request, response);
            buildCobrandCookie(request, sessionContext, response);
            determineAbVersion(trno, request, sessionContext);
        }

        return true;
    }

    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse response, Object o, ModelAndView modelAndView) throws Exception {
        //do nothing
    }

    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        //do nothing
    }


    protected void buildCobrandCookie(HttpServletRequest request, SessionContext sessionContext, HttpServletResponse response) {
        Cookie cobrandCookie = findCookie(request, SessionContextUtil.COBRAND_COOKIE);
        String hostName = sessionContext.getHostName();
        if (cobrandCookie == null || !hostName.equals(cobrandCookie.getValue())) {
            cobrandCookie = new Cookie(SessionContextUtil.COBRAND_COOKIE, hostName);
            cobrandCookie.setPath("/");
            cobrandCookie.setDomain(".greatschools.net");
            response.addCookie(cobrandCookie);
        }
    }

    protected Cookie buildTrnoCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie trno = findCookie(request, SessionContextUtil.TRNO_COOKIE);

        if (trno == null) {
            String ipAddress = request.getHeader("x_forwarded_for");

            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }

            String cookieValue = String.valueOf(System.currentTimeMillis() / 1000) + "." + ipAddress;

            //cookie expires approx. two years from now
            trno = new Cookie(SessionContextUtil.TRNO_COOKIE, cookieValue);
            trno.setPath("/");
            trno.setMaxAge(63113852);
            response.addCookie(trno);
        }
        return trno;
    }

    /**
     * Reads AB configuration from file and attempts to parse it. In the event of any error or
     * invalid configuration, this defaults to 50/50 testing.
     * This method should only be called once in the lifetime of the JVM, to populate the static
     * array of configuration data _abCutoffs.
     */
    protected static void readABConfiguration() {
        Resource resource = new ClassPathResource(AB_CONFIGURATION_FILE_CLASSPATH);
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            String line = reader.readLine();
            while (StringUtils.isNotEmpty(line)) {
                if (line.startsWith("#")) {
                    // comment line, ignore
                } else {
                    buffer.append(line);
                }
                line = reader.readLine();
            }
            convertABConfigToArray(buffer.toString());
        } catch (Exception e) {
            _abCutoffs = null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        if (_abCutoffs == null) {
            _log.error("Couldn't determine ab version from file " + AB_CONFIGURATION_FILE_CLASSPATH);
            _abCutoffs = new int[] {1, 1};
            _cutoffTotal = 2;
        }
    }

    /**
     * This converts an AB configuration string as read from file into a static array that can
     * be used elsewhere to determine the AB version. If there are any errors, this sets _abCutoffs
     * to null.
     * @param abConfiguration read from file
     * @throws NumberFormatException if the configuration has non-numeric values
     */
    protected static void convertABConfigToArray(String abConfiguration) throws NumberFormatException {
        if (StringUtils.isEmpty(abConfiguration)) {
            return;
        }
        // split on forward slash '/'
        StringTokenizer tok = new StringTokenizer(abConfiguration, "/");

        // Restrict number of tokens to range 2-26
        if (tok.countTokens() > 1 && tok.countTokens() < 27) {
            _cutoffTotal = 0;
            _abCutoffs = new int[tok.countTokens()];
            int tokenNum = 0;
            // place the tokens in the array _abCutoffs and set _cutoffTotal to the sum of the tokens
            while (tok.hasMoreTokens()) {
                int num = Integer.valueOf(tok.nextToken());
                // check for invalid values
                if (num < 1 || num > 99) {
                    _abCutoffs = null;
                    break;
                }
                _cutoffTotal += num;
                // check for invalid values
                if (_cutoffTotal > 100) {
                    _abCutoffs = null;
                    break;
                }
                _abCutoffs[tokenNum++] = num;
            }
        }
    }

    protected void determineAbVersion(Cookie trno, HttpServletRequest request, SessionContext sessionContext) {
        // Set the a/b version - 'a' is the default
        String versionParam = request.getParameter("version");
        if (StringUtils.isNotBlank(versionParam)) {
            // version override takes precedence
            sessionContext.setAbVersion(versionParam.trim());
        } else if (isKnownCrawler(request)) {
            // GS-4614 Ensure crawlers always see the A version in multivariant tests
            sessionContext.setAbVersion("a");
        } else {
            long trnoSecondsSinceEpoch = 0;
            String trnoValue = trno.getValue();
            try {
                // Extract the time from the TRNO (180654739.127.0.0.1 => 180654739)
                trnoSecondsSinceEpoch = Long.valueOf(trnoValue.substring(0, trnoValue.indexOf(".")));
            } catch (Exception e) {
                // do nothing
            }
            // Use the time from when TRNO was set to determine what variant the user should get
            determineVariantFromConfiguration(trnoSecondsSinceEpoch, sessionContext);
        }
    }

    protected boolean isKnownCrawler(HttpServletRequest request) {
        return SessionContextUtil.isKnownCrawler(request.getHeader("User-Agent"));
    }

    protected void determineVariantFromConfiguration(long trnoSecondsSinceEpoch, SessionContext sessionContext) {
        if (_abCutoffs == null) {
            // read configuration from file if necessary
            readABConfiguration();
        }

        char abVersion = 'a';
        int runningTotal = 0;
        // Check each cutoff to see if the trno falls into it, if so assign the ab version appropriately.
        // AB version starts at 'a' and goes up the alphabet from there, one letter per cutoff.
        // EXAMPLE:
        // cutoffs = [1, 1]; total = 2;
        // First, set runningTotal to 1 (value of first cutoff).
        // Check if trnoSecondsSinceEpoch % 2 < 1 (this has 50% chance of being true)
        // if so set version to 'a' otherwise continue
        // Next iteration, set runningTotal to 2 (previous value plus second cutoff)
        // Check if trnoSecondsSinceEpoch % 2 < 2 (always true)
        // set version to 'b'
        for (int num: _abCutoffs) {
            runningTotal += num;
            if (trnoSecondsSinceEpoch % _cutoffTotal < runningTotal) {
                sessionContext.setAbVersion(Character.toString(abVersion));
                break;
            }
            // increment ab version (e.g. from 'a' to 'b')
            abVersion++;
        }
    }

    public static String convertABConfigurationToString() {
        StringBuffer title = new StringBuffer();
        StringBuffer percents = new StringBuffer();

        if (_abCutoffs == null) {
            // read configuration from file if necessary
            readABConfiguration();
        }

        char abVersion = 'a';
        for (int i = 0; i < _abCutoffs.length; i++) {
            int num = _abCutoffs[i];
            title.append(Character.toUpperCase(abVersion));
            float percent = ((float) num) / ((float) _cutoffTotal) * 100;
            percents.append((int) percent);
            abVersion++;
            if (i < _abCutoffs.length - 1) {
                title.append('/');
                percents.append('/');
            }
        }

        return title.toString() + ": " + percents.toString();
    }

    protected Cookie findCookie(HttpServletRequest request, String cookieName) {
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }
}
