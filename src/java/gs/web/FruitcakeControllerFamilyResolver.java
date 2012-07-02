package gs.web;

import gs.data.school.School;
import gs.data.school.SchoolHelper;
import gs.web.request.RequestAttributeHelper;
import gs.web.request.RequestInfo;
import gs.web.util.CookieUtil;
import gs.web.util.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class FruitcakeControllerFamilyResolver implements IControllerFamilyResolver {
    @Autowired private RequestAttributeHelper _requestAttributeHelper;

    public ControllerFamily resolveControllerFamily() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        ControllerFamily family = null;

        if (request == null) {
            throw new IllegalStateException("Request cannot be null.");
        }

        boolean fruitcakeEnabled = UrlUtil.isDeveloperWorkstation(request.getServerName()) ||
                (UrlUtil.isDevEnvironment(request.getServerName()) && !UrlUtil.isQAServer(request.getServerName()));

        if (!fruitcakeEnabled && UrlUtil.isQAServer(request.getServerName())) {
            Cookie cookie = CookieUtil.getCookie(request, RequestInfo.FRUITCAKE_ENABLED_COOKIE_NAME);

            if (cookie != null && Boolean.TRUE.equals(Boolean.valueOf(cookie.getValue()))) {
                fruitcakeEnabled = true;
            }
        }

        if (fruitcakeEnabled) {
            School school = _requestAttributeHelper.getSchool(request);
            if (school != null && school.isActive()) {
                // for school pages, serve fruitcake version only if the school is in local and is not a preschool
                if (!school.isPreschoolOnly() && SchoolHelper.isSchoolInLocal(school)) {
                    family = ControllerFamily.FRUITCAKE;
                }
            } else if (school == null) {
                // for non-school pages (like search), it's true as long as fruitcakeEnabled is true
                family = ControllerFamily.FRUITCAKE;
            }
        }
        return family;
    }
}
