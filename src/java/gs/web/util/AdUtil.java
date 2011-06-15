package gs.web.util;

import gs.data.state.State;
import gs.data.state.StateManager;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// sample code
// String referrer = request.getHeader("Referer");
// String hostname = request.getServerName();
// model.put(MODEL_K12_CLICKTHROUGH_URL, AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenter.getUri()));

public class AdUtil {
    final private static StateManager STATE_MANAGER = new StateManager();

    final private static String K12_CLICK_THROUGH_URL_PREFIX = "http://ww2.k12.com/cm/?affl=gr8t";
    final private static Pattern K12_POSSIBLE_CITY_HOME_REGEX = Pattern.compile("^/(.*?)/(.*?)/$");
    final private static Pattern K12_POSSIBLE_STATE_HOME_REGEX = Pattern.compile("^/(.*?)/$");
    final private static Pattern K12_TOPIC_CENTER_URI_REGEX = Pattern.compile("^online-education-([a-z]{2,3})$");
    /**
     * Return the click-through url to use on ads to send users to specific pages of the k12.com site
     * @param referrer e.g. http://www.greatschools.org/search/search.page?search_type=0&q=san+francisco&state=CA&c=school
     * @param hostname e.g. www.greatschools.org
     * @param k12School e.g. CA, INT
     * @return url
     */
    public static String getK12ClickThroughUrl(String referrer, String hostname, String k12School) {
        boolean referredByAnotherSite =
                StringUtils.isBlank(referrer) ||
                StringUtils.isBlank(hostname) ||
                !referrer.replaceFirst("^https?://","").startsWith(hostname);
        String requestUri = (StringUtils.isNotBlank(referrer) ? referrer.replaceFirst("^https?://.*?/","/").replaceFirst("\\?.*$", "") : null);

        Matcher cityHomeMatcher = (requestUri != null ? K12_POSSIBLE_CITY_HOME_REGEX.matcher(requestUri) : null);
        Matcher stateHomeMatcher = (requestUri != null ? K12_POSSIBLE_STATE_HOME_REGEX.matcher(requestUri) : null);

        String page = "ot";
        if (!referredByAnotherSite) {
            if (requestUri.equals("/search/search.page") ||
                    requestUri.equals("/search/nearbySearch.page") ||
                    requestUri.endsWith("/schools/") ||
                    requestUri.endsWith("/preschools/") ||
                    requestUri.endsWith("/elementary-schools/") ||
                    requestUri.endsWith("/middle-schools/") ||
                    requestUri.endsWith("/high-schools/")) {
                page = "sr";
            } else if (cityHomeMatcher.find()) { //(requestUri.matches("^/.*?/.*?/$")) {
                // /[possible state long name]/[any string]/
                State state = STATE_MANAGER.getStateByLongName(cityHomeMatcher.group(1));
                if (state != null) {
                    page = "cp";
                }
            } else if (stateHomeMatcher.find()) {
                // /[possible state long name]/
                State state = STATE_MANAGER.getStateByLongName(stateHomeMatcher.group(1));
                if (state != null) {
                    page = "rc";
                }
            }
            // TODO-11730 traffic driver for school overview page url
            // e.g. http://dev.greatschools.org/california/fresno/842-Lowell-Elementary-School/
            // use page=so
        }

        String school = (StringUtils.isNotBlank(k12School) ? k12School : "INT");
        if (!"INT".equals(school) && STATE_MANAGER.getState(school) == null) {
            school = "INT";
        }

        return K12_CLICK_THROUGH_URL_PREFIX + "&page=" + page + "&school=" + school;
    }
}
