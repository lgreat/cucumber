package gs.web.util.context;

import gs.web.util.UrlUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * Accesses "subcookies" that is consistent with the way sub cookie handling is done in javascript
 * Created by IntelliJ IDEA.
 * User: jnorton
 * Date: Jun 3, 2008
 * Time: 9:33:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class SubCookie {

    private String cookieName;
    private static final String nameValueSeparator = "$$:$$";
	private static final String subcookieSeparator = "$$/$$";
    private static final String nameValueSeparatorRegEx = "\\$\\$:\\$\\$";
	private static final String subcookieSeparatorRegEx = "\\$\\$/\\$\\$";
    private Map<String, String> properties;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private UrlUtil urlUtil = new UrlUtil();



    public SubCookie(String cookieName, HttpServletRequest request, HttpServletResponse response ){
        this.cookieName = cookieName;
        this.request = request;
        this.response = response;
        readCookie();
    }

    public void setProperty(String property, String value){
        properties.put(property, value);
        writeCookie();
    }

    public Object getProperty(String property){
        return properties.get(property);
    }

    public void removeProperty(String property){
        properties.remove(property);
        writeCookie();
    }

    protected void readCookie(){
        properties = new HashMap<String, String>();
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return;
        }
        for (Cookie thisCookie : cookies) {
            if (cookieName.equals(thisCookie.getName())){
                properties = decodeProperties(thisCookie);
            }
        }
    }


    protected String getDomain(){
        if (urlUtil.isDeveloperWorkstation(request.getServerName())) {
            // don't set domain for developer workstations
            // so they can still access the cookie!!
            return null;
        } else {
            return ".greatschools.net";
        }
    }

    protected void writeCookie(){
        String cookieValue = encodeProperties(this.properties);
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setDomain(getDomain());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * this is testable!
     * @return a string with the properties coded into the cookie value
     */
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
        Map<String, String> decodedProperties = new HashMap<String, String>();
        String value = cookie.getValue();
        String[] keyValuePairs = value.split(subcookieSeparatorRegEx);

        if (keyValuePairs == null || value.equals("")){
            return decodedProperties;
        }

        for(String keyValuePair: keyValuePairs){
            String[] pair = keyValuePair.split(nameValueSeparatorRegEx);
            if( pair.length == 2) {
                decodedProperties.put(pair[0], pair[1]);
            } // otherwise throw an exception?
        }
        return decodedProperties;
    }
}
