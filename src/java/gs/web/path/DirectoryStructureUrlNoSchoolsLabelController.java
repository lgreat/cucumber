package gs.web.path;

import gs.web.util.UrlBuilder;
import gs.data.school.SchoolType;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Set;

/**
 * If request uri is in the form /[state long name]/[city name]/, redirect to /[state long name]/[city name]/schools/.
 * If request uri is in the form /[state long name]/[city name]/[school type label]/, redirect to /[state long name]/[city name]/[school type label]/schools/
 * @author Young Fan
 */
public class DirectoryStructureUrlNoSchoolsLabelController extends AbstractController implements IDirectoryStructureUrlController {

    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);

        Set<SchoolType> schoolTypes;
        if (fields.hasSchoolTypes()) {
            schoolTypes = fields.getSchoolTypes();
        } else {
            schoolTypes = new HashSet<SchoolType>();
        }

        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, fields.getState(), fields.getCityName(), schoolTypes, null);
        return new ModelAndView(new RedirectView(urlBuilder.asSiteRelative(request)));
    }

    // required to implement IDirectoryStructureUrlController
    public boolean shouldHandleRequest(DirectoryStructureUrlFields fields) {
        if (fields == null) {
            return false;
        }
        return fields.hasState() && fields.hasCityName() && !fields.hasSchoolsLabel();
    }    
}
