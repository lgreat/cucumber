package gs.web.compare;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CompareOverviewController extends AbstractCompareSchoolController {
    public static final String TAB_NAME = "overview";
    private String _successView;

    @Override
    protected void handleCompareRequest(HttpServletRequest request, HttpServletResponse response,
                                        List<ComparedSchoolBaseStruct> schools, Map<String, Object> model)
            throws IOException {
        model.put(MODEL_TAB, TAB_NAME);
        handleGSRating(request, schools);
        handleCommunityRating(schools);
        handleRecentReview(schools);
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
