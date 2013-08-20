package gs.web;

/**
 * Created with IntelliJ IDEA.
 * User: sarora
 * Date: 8/16/13
 * Time: 3:29 PM
 * To change this template use File | Settings | File Templates.
 */

import gs.data.school.School;
import gs.data.school.SchoolHelper;
import gs.web.request.RequestAttributeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * City Hub Controller Family Resolver.
 * @author Shomi Arora sarora@greatschools.org
 */
public class CityHubControllerFamilyResolver  implements IControllerFamilyResolver{

    @Autowired
    private RequestAttributeHelper _requestAttributeHelper;

    public ControllerFamily resolveControllerFamily() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        ControllerFamily family = null;
        /**
         * This Code should change to add the logic for city pages .Possibly string match on URL. -To DO Shomi Revert
         */
//        if (request == null) {
//            throw new IllegalStateException("Request cannot be null.");
//        }
//
//        School school = _requestAttributeHelper.getSchool(request);
//        if (school != null) {
//            // for school pages, delegate this logic to the SchoolHelper
//            if (SchoolHelper.isSchoolForNewProfile(school)) {
//                family = ControllerFamily.FRUITCAKE;
//            }
//        }
        return ControllerFamily.CITY_DEFAULT;
    }
}
