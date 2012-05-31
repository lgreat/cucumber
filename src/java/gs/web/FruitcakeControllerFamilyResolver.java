package gs.web;

import gs.data.community.User;
import gs.data.security.Role;
import gs.web.request.RequestInfo;
import gs.web.util.CookieUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class FruitcakeControllerFamilyResolver implements IControllerFamilyResolver {
    @Autowired
    private ApplicationContext _applicationContext;

    public ControllerFamily resolveControllerFamily() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        ControllerFamily family = null;

        if (request == null) {
            throw new IllegalStateException("Request cannot be null.");
        }

        Cookie cookie = CookieUtil.getCookie(request, RequestInfo.FRUITCAKE_ENABLED_COOKIE_NAME);

        if (cookie != null && Boolean.TRUE.equals(Boolean.valueOf(cookie.getValue()))) {
            family = ControllerFamily.FRUITCAKE;
        }

        return family;
    };

    public ApplicationContext getApplicationContext() {
        return _applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        _applicationContext = applicationContext;
    }
}
