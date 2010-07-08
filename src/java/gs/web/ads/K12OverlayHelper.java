package gs.web.ads;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Jul 7, 2010
 * Time: 8:41:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class K12OverlayHelper {
    final private static String COOKIE_KEY = "k12Overlay";

    public static boolean hasK12OverlayCookie(HttpServletRequest request, HttpServletResponse response) {
        K12OverlayCookie cookie = new K12OverlayCookie(request, response);
        return !StringUtils.isEmpty(cookie.getProperty(COOKIE_KEY));
    }

    // TODO-10239 implement date range
    public static boolean isInK12OverlayDateRange(Date currentDate) {
        return true;
    }

    /**
     * Return whether to show the K12 site overlay ad, based on a cookie check and a date check.
     * Warning: This does not make the necessary ad-free and co-brand checks.
     * @return true if the ad should be shown; false otherwise
     */
    public static boolean isShowK12Overlay(HttpServletRequest request, HttpServletResponse response) {
        // cookie check and date check
        return !K12OverlayHelper.hasK12OverlayCookie(request,response) &&
                K12OverlayHelper.isInK12OverlayDateRange(new Date());
    }
}
