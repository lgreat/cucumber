package gs.web.school;

import gs.data.school.EspResponse;
import gs.data.school.SchoolMedia;
import gs.data.util.CommunityUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/school/profileCulture.page")
public class SchoolProfileCultureController extends AbstractSchoolProfileController {
    private static final Log _log = LogFactory.getLog(SchoolProfileCultureController.class);
    public static final String VIEW = "school/profileCulture";

    @Autowired
    private SchoolProfileDataHelper _schoolProfileDataHelper;

    @RequestMapping(method = RequestMethod.GET)
    public String getCulturePage(ModelMap modelMap, HttpServletRequest request) {
        return VIEW;
    }

    private static final String [] MODEL_PREFIXES = {"culture"};

    private static final List<SchoolProfileDisplayBean> DISPLAY_CONFIG = new ArrayList<SchoolProfileDisplayBean>();
    static {
        buildCultureDisplayStructure();
    }

    protected void getCultureDetails(ModelMap modelMap,HttpServletRequest request) {
        Map<String, List<EspResponse>> espResults = _schoolProfileDataHelper.getEspDataForSchool(request);
        Map<String, List<String>> resultsModel = SchoolProfileProgramsController.buildDisplayData( espResults, DISPLAY_CONFIG);
        modelMap.put( "cultureProfileData", resultsModel );
        SchoolProfileProgramsController.buildDisplayModel(MODEL_PREFIXES, resultsModel, DISPLAY_CONFIG, modelMap );
        getSchoolPhotos(modelMap,request);
    }

    protected void getSchoolPhotos(ModelMap modelMap,HttpServletRequest request) {
        List<SchoolMedia> photoGalleryImages = _schoolProfileDataHelper.getSchoolMedia(request);
        modelMap.put("basePhotoPath", CommunityUtil.getMediaPrefix());
        modelMap.put("photoGalleryImages", photoGalleryImages);
    }

    private static void buildCultureDisplayStructure() {
        String tabAbbrev = "culture";
        String sectionAbbrev = "SchoolCulture";
        String sectionTitle = "School Culture";
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Dress Code",
                "dress_code" ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Bullying Policy",
                "bullying_policy" ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Parent Involvement",
                "parent_involvement" ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Parent Involvement other",
                "parent_involvement_other" ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "School Colors",
                "school_colors" ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "School Mascot",
                "school_mascot" ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Anything else",
                "anything_else" ) );
    }
}