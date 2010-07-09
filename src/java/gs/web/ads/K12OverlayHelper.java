package gs.web.ads;

import gs.web.util.PageHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

    final private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    // 7/12, 7/13, 7/19, 7/20, 7/26, 7/27, 8/2, 8/9, 8/16, 8/23, 8/30
    final private static Set<String> k12DateRange = new HashSet<String>();
    static {
        // TODO-10239 remove me!
        k12DateRange.add("2010-07-09"); // for testing only!

        k12DateRange.add("2010-07-12"); // 7/12
        k12DateRange.add("2010-07-13"); // 7/13
        k12DateRange.add("2010-07-19"); // 7/19
        k12DateRange.add("2010-07-20"); // 7/20
        k12DateRange.add("2010-07-26"); // 7/26
        k12DateRange.add("2010-07-27"); // 7/27
        k12DateRange.add("2010-08-02"); // 8/2
        k12DateRange.add("2010-08-09"); // 8/9
        k12DateRange.add("2010-08-16"); // 8/16
        k12DateRange.add("2010-08-23"); // 8/23
        k12DateRange.add("2010-08-30"); // 8/30
    }

    // The overlay should only be active on the following dates:
    // 7/12, 7/13, 7/19, 7/20, 7/26, 7/27, 8/2, 8/9, 8/16, 8/23, 8/30 (12a-11:59p PT)
    public static boolean isInK12OverlayDateRange(Date dateToCheck) {
        String dateToCheckStr = df.format(dateToCheck);
        return k12DateRange.contains(dateToCheckStr);
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

    // Preschool editorial and landing page (any page with GA_googleAddAttr('editorial','Preschool');)
    // Choose the right school editorial and landing page (any page with GA_googleAddAttr('editorial','Findaschoo');)
    // Academics and activities editorial and landing page (any page with GA_googleAddAttr('editorial','AcademicsA');)
    // http://www.greatschools.org/back-to-school/supplies/supply-list-to-get-you-started.gs?content=109
    // http://www.greatschools.org/back-to-school/supplies/k-2-back-to-school-supply-list.gs?content=1082
    //
    // URLs for landing pages that should be excluded (included in above categories):
    // http://www.greatschools.org/preschool/
    // http://www.greatschools.org/students.topic?content=1540
    // http://www.greatschools.org/school-choice/
    final private static Set<String> editorialValuesToExclude = new HashSet<String>();
    static {
        editorialValuesToExclude.add("Preschool");
        editorialValuesToExclude.add("AcademicsA");
        editorialValuesToExclude.add("Findaschoo");
    }

    final private static Set<String> articleIdValuesToExclude = new HashSet<String>();
    static {
        articleIdValuesToExclude.add("109"); // articleIdValuesToExclude
        articleIdValuesToExclude.add("1082");
    }

    final private static String GAM_EDITORIAL_KEYWORD = "editorial";
    final private static String GAM_ARTICLE_ID_KEYWORD = "article_id";

    public static boolean isExcludeFromK12Overlay(PageHelper pageHelper) {
        boolean excludeFromK12Overlay = false;
        if (pageHelper != null) {
            MultiMap adKeywords = pageHelper.getAdKeywords();
            if (adKeywords != null) {
                Collection<String> values = (Collection<String>) adKeywords.get(GAM_EDITORIAL_KEYWORD);
                if (values != null) {
                    Collection intersection = CollectionUtils.intersection(editorialValuesToExclude, values);
                    if (intersection != null && intersection.size() > 0) {
                        excludeFromK12Overlay = true;
                    }
                }

                if (!excludeFromK12Overlay) {
                    values = (Collection<String>) adKeywords.get(GAM_ARTICLE_ID_KEYWORD);
                    if (values != null) {
                        Collection intersection = CollectionUtils.intersection(articleIdValuesToExclude, values);
                        if (intersection != null && intersection.size() > 0) {
                            excludeFromK12Overlay = true;
                        }
                    }
                }
            }
        }
        return excludeFromK12Overlay;
    }
}
