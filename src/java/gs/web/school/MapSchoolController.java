package gs.web.school;

import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.NearbySchool;

import java.util.List;
import java.util.ArrayList;

/**
 * Provides ...
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class MapSchoolController extends AbstractSchoolController {
    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);

        List<NearbySchool> nearbySchools = getSchoolDao().findNearbySchools(school, 5);
        List<School> mapSchools = new ArrayList<School>();
        for (NearbySchool nearbySchool : nearbySchools) {
            mapSchools.add(nearbySchool.getNeighbor());
        }
        request.setAttribute("mapSchools", mapSchools);
        request.setAttribute("nearbySchools", nearbySchools);
        request.setAttribute("hasNearby", (nearbySchools.size() > 0));
        request.setAttribute("levelLongName", school.getLevelCode().getLowestLevel().getLongName());


        return new ModelAndView(_viewName);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
