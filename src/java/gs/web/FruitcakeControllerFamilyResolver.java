package gs.web;

import gs.data.community.User;
import gs.data.security.Role;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;

public class FruitcakeControllerFamilyResolver implements IControllerFamilyResolver {
    @Autowired
    public SessionContextUtil _sessionContextUtil;

    public ControllerFamily resolveControllerFamily() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        ControllerFamily family = null;

        if (request == null) {
            throw new IllegalStateException("Request cannot be null.");
        }

        SessionContext context = _sessionContextUtil.guaranteeSessionContext(request);
        _sessionContextUtil.readCookies(request, context);

        if (context != null) {
            User user = SessionContextUtil.getSessionContext(request).getUser();

            if (user != null && user.hasRole(Role.FRUITCAKE_MEMBER)) {
                family = ControllerFamily.FRUITCAKE;
            }
        }

        return family;
    };

}
