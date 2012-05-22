package gs.web;

import gs.data.community.User;
import gs.data.security.Role;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;

public class FruitcakeControllerFamilyResolver implements IControllerFamilyResolver {
    @Autowired
    private SessionContextUtil _sessionContextUtil;

    @Autowired
    private ApplicationContext _applicationContext;

    public ControllerFamily resolveControllerFamily() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        ControllerFamily family = null;

        if (request == null) {
            throw new IllegalStateException("Request cannot be null.");
        }

        SessionContext context = (SessionContext) _applicationContext.getBean(SessionContext.BEAN_ID);
        _sessionContextUtil.readCookies(request, context);

        if (context != null) {
            User user = context.getUser();

            if (user != null && user.hasRole(Role.FRUITCAKE_MEMBER)) {
                family = ControllerFamily.FRUITCAKE;
            }
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
