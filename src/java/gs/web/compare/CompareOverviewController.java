package gs.web.compare;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CompareOverviewController extends AbstractCompareSchoolController {
    private String _successView;

    @Override
    protected void handleCompareRequest(HttpServletRequest request, HttpServletResponse response,
                                        List<ComparedSchoolBaseStruct> schools, Map<String, Object> model)
            throws IOException {
        model.put(MODEL_TAB, "overview");
        handleGSRating(request, schools);
    }

    @Override
    protected ComparedSchoolBaseStruct getStruct() {
        return new ComparedSchoolOverviewStruct();
    }

    @Override
    public String getSuccessView() {
        return _successView;
    }

    public void setSuccessView(String successView) {
        _successView = successView;
    }
}
