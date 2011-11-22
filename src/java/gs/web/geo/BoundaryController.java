package gs.web.geo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
@Controller
@RequestMapping(value="/geo/boundary/", method=RequestMethod.GET)
public class BoundaryController {
    public final static String BOUNDARY_VIEW_NAME = "geo/districtBoundary";

    @RequestMapping(value="boundary.page")
    public String getPage(ModelMap model) {
        return BOUNDARY_VIEW_NAME;
    }
}
