package gs.web.school;

import gs.data.school.EspResponse;
import gs.data.school.School;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 8/20/12
 * Time: 11:41 AM
 * To change this template use File | Settings | File Templates.
 */

@Controller
@RequestMapping("/school/profileEnrollment.page")
public class SchoolProfileEnrollmentController extends AbstractSchoolProfileController {
    protected static final Log _log = LogFactory.getLog(SchoolProfileEnrollmentController.class.getName());
    public static final String VIEW = "school/profileEnrollment";

    private static final String APPL_INFO_MODEL_KEY = "applInfo";

    @Autowired
    private SchoolProfileDataHelper _schoolProfileDataHelper;

    @RequestMapping(method = RequestMethod.GET)
    public String handle(ModelMap modelMap, HttpServletRequest request) {
        School school = getSchool(request);
        modelMap.put( "school", school );

        // There are two versions of this page, one if there is OSP (aka ESP) data available or not.
        // This can be determined by retrieving the esp data and seeing it if any data is returned.
        // Then execute the Esp or non-Esp code
        Map<String, List<EspResponse>> espResults = _schoolProfileDataHelper.getEspDataForSchool( request );
        modelMap.put( "espData", espResults );

        if( espResults != null && !espResults.isEmpty() ) {
            // OSP case
            handleEspPage( modelMap, request, school, espResults );
        }
        else {
            // Non-OSP case
//            handleNonEspPage( modelMap, request, school );
        }
        return VIEW;
    }

    private void handleEspPage(Map model, HttpServletRequest request, School school, Map<String,List<EspResponse>> espData) {
//        model.put( APPL_INFO_MODEL_KEY, getApplInfoEspTile(request, school, espData) );
    }
}
