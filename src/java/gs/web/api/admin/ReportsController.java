package gs.web.api.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author chriskimm@greatschools.net
 */
@Controller
@RequestMapping("/api/admin/reports.page")
public class ReportsController {

    public static final String REPORTS_VIEW_NAME = "/api/admin/reports";

    @RequestMapping(method = RequestMethod.GET)
    public String getPage() {
        return REPORTS_VIEW_NAME;
    }
}
