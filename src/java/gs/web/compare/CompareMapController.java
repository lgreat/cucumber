package gs.web.compare;

import gs.data.geo.LatLonRect;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CompareMapController extends AbstractCompareSchoolController {
    public static final String TAB_NAME = "map";
    private String _successView;
    public static final String MODEL_MAP_CENTER = "mapCenter";
    public static final String PARAM_SELECTED_SCHOOL = "selectedSchool";

    @Override
    protected void handleCompareRequest(HttpServletRequest request, HttpServletResponse response,
                                        List<ComparedSchoolBaseStruct> schools, Map<String, Object> model) throws
                                                                                                           IOException {
        model.put(MODEL_TAB, TAB_NAME);

        handleGSRating(request, schools);

        determineCenterOfMap(schools, model);

        handleCommunityRating(schools);

        // if a school is requested to be selected, set the field on that school
        if (request.getParameter(PARAM_SELECTED_SCHOOL) != null) {
            for (ComparedSchoolBaseStruct struct: schools) {
                if (StringUtils.equals(request.getParameter(PARAM_SELECTED_SCHOOL), struct.getUniqueIdentifier())) {
                    ((ComparedSchoolMapStruct) struct).setSelected(true);
                }
            }
        }
    }

    /**
     * Use the constructor of LatLonRect to determine the center of the list of schools.
     */
    protected void determineCenterOfMap(List<ComparedSchoolBaseStruct> schools, Map<String, Object> model) {
        LatLonRect latLonRect = new LatLonRect(schools);
        model.put(MODEL_MAP_CENTER, latLonRect.getCenter());
    }

    @Override
    protected ComparedSchoolBaseStruct getStruct() {
        return new ComparedSchoolMapStruct();
    }

    @Override
    public String getSuccessView() {
        return _successView;
    }

    public void setSuccessView(String successView) {
        _successView = successView;
    }

    @Override
    public int getPageSize() {
        return 8;
    }

    @Override
    protected String getTabName() {
        return TAB_NAME;
    }

}
