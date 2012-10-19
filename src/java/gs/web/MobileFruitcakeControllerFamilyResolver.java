package gs.web;

import gs.data.school.School;
import gs.data.school.SchoolHelper;
import gs.web.request.RequestAttributeHelper;
import gs.web.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class MobileFruitcakeControllerFamilyResolver implements IControllerFamilyResolver {
    @Autowired private RequestAttributeHelper _requestAttributeHelper;

    public ControllerFamily resolveControllerFamily() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        ControllerFamily family = null;

        if (request == null) {
            throw new IllegalStateException("Request cannot be null.");
        }

        RequestInfo requestInfo =  RequestInfo.getRequestInfo(request);
        if (requestInfo == null) {
            throw new IllegalStateException("requestInfo was null.");
        }

        School school = _requestAttributeHelper.getSchool(request);
        if (school != null) {
            // for school pages, delegate this logic to the SchoolHelper
            if (requestInfo.shouldRenderMobileView() && SchoolHelper.isSchoolForNewProfile(school)) {
                family = ControllerFamily.MOBILE_FRUITCAKE;
            }
        }
        return family;
    }

    /** For unit testing */
    public void setRequestAttributeHelper(RequestAttributeHelper requestAttributeHelper) {
        _requestAttributeHelper = requestAttributeHelper;
    }
    public RequestAttributeHelper getRequestAttributeHelper() {
        return _requestAttributeHelper;
    }
}
