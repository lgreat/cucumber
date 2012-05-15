package gs.web.util.context;

import gs.web.util.UrlUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.util.CookieGenerator;

/**
 * Accesses "subcookies" that is consistent with the way sub cookie handled is done in javascript for omniture
 * Created by IntelliJ IDEA.
 * User: jnorton
 * Date: Jun 3, 2008
 * Time: 9:33:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class SubCookie {
    private static final Log _log = LogFactory.getLog(SubCookie.class);

    private static final String nameValueSeparator = "$$:$$";
	private static final String subcookieSeparator = "$$/$$";
    private static final String nameValueSeparatorRegEx = "\\$\\$:\\$\\$";
	private static final String subcookieSeparatorRegEx = "\\$\\$/\\$\\$";
    private Map<String, String> properties;
    private HttpServletRequest request;
    private HttpServletResponse response;

    public SubCookie(HttpServletRequest request, HttpServletResponse response){
        this.request = request;
        this.response = response;
        readCookie();
    }

    public void setProperty(String property, String value){
        properties.put(property, value);
        writeCookie();
    }

    public String getProperty(String property){
        return properties.get(property);
    }

    public void removeProperty(String property){
        properties.remove(property);
        writeCookie();
    }

    protected void readCookie(){
        properties = new HashMap<String, String>();

        CookieGenerator generator = getCookieGenerator();
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return;
        }
        for (Cookie thisCookie : cookies) {
            if (generator.getCookieName().equals(thisCookie.getName())){
                properties = decodeProperties(thisCookie);
            }
        }
    }

    protected CookieGenerator getCookieGenerator() {
        return SessionContextUtil
                .getSessionContext(request)
                .getSessionContextUtil()
                .getOmnitureSubCookieGenerator();
    }

    protected void writeCookie() {
        String cookieValue = encodeProperties(this.properties);

        try {
            cookieValue = URIUtil.encodeWithinQuery(cookieValue);
        } catch (URIException e) {
            _log.warn("Unable to encode parameter");
        }
        CookieGenerator generator = getCookieGenerator();

        if (UrlUtil.isDeveloperWorkstation(request.getServerName())) {
            // don't set domain for developer workstations
            // so they can still access the cookie!!
            generator.setCookieDomain(null);
        } else {
            generator.setCookieDomain(".greatschools.org");
        }
        _log.error("setting sub cookie: " + cookieValue + ", " + generator.getCookieName() + ", " +
                "" + generator.getCookieDomain());
        generator.addCookie(response,cookieValue);
    }

    protected static String encodeProperties(Map<String, String> props) {
        StringBuilder sb = new StringBuilder();

        Iterator<String> iterator = props.keySet().iterator();
        while (iterator.hasNext()){
            String key = iterator.next();
            sb.append(key);
            sb.append(nameValueSeparator);
            sb.append(props.get(key));
            if (iterator.hasNext()){
                sb.append(subcookieSeparator);
            }
        }
        return sb.toString();
    }

    protected static Map<String, String> decodeProperties(Cookie cookie) {
        String value = cookie.getValue();
        Map<String, String> decodedProperties = new HashMap<String, String>();

        _log.debug("SubCookie.decodeProperties: " + value);


        try {
            value = URIUtil.decode(value);
        } catch (URIException e) {
            _log.warn("Unable to decode cookie");
        }

        _log.debug("URLDecoded value: " + value);

        if (value == null || value.equals("")){
            _log.debug("early departure, cookie value is: " + value);
            return decodedProperties;    
        }

        String[] keyValuePairs = value.split(subcookieSeparatorRegEx);

        if (keyValuePairs == null ){
            _log.debug("early departure, keyValuePairs is: null");
            return decodedProperties;
        }

        for(String keyValuePair: keyValuePairs){
            String[] pair = keyValuePair.split(nameValueSeparatorRegEx);

            if( pair.length == 2) {
                _log.debug(pair[0] + ", " + pair[1]);
                decodedProperties.put(pair[0], pair[1]);
            }else{
                _log.error("Didn't expect an non pair: length = " + pair.length );
                for (String s : pair){
                    _log.error(s);
                }
            }
        }
        _log.debug("normal exit");
        return decodedProperties;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }
}
