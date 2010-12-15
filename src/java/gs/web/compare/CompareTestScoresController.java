package gs.web.compare;

import gs.data.school.School;
import gs.data.state.State;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CompareTestScoresController extends AbstractCompareSchoolController {
    private final Log _log = LogFactory.getLog(getClass());
    public static final String TAB_NAME = "testScores";
    public static final String COMPARE_CONFIG_TAB_NAME = "test";
    public static final String MODEL_TEST_ROWS = "testRows";
    private String _successView;

    @Override
    protected void handleCompareRequest(HttpServletRequest request, HttpServletResponse response,
                                        List<ComparedSchoolBaseStruct> structs, Map<String, Object> model) throws
                                                                                                           Exception {
        model.put(MODEL_TAB, TAB_NAME);

        if (structs.size() == 0) {
            return;
        }

        handleGSRating(request, structs);

        List<School> schools = new ArrayList<School>(structs.size());
        for (ComparedSchoolBaseStruct baseStruct: structs) {
            schools.add(baseStruct.getSchool());
        }

        model.put(MODEL_TEST_ROWS, getSchoolTestData(schools.get(0).getDatabaseState(), schools,
                                                     COMPARE_CONFIG_TAB_NAME));

    }

    protected List<Object[]> getSchoolTestData(State state, List<School> schools, String tabName) {
        return new ArrayList<Object[]>();
    }

    @Override
    public String getSuccessView() {
        return _successView;
    }

    public void setSuccessView(String successView) {
        _successView = successView;
    }

    @Override
    protected ComparedSchoolBaseStruct getStruct() {
        return new ComparedSchoolBaseStruct();
    }
}
