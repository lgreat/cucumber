package gs.web.school;

import gs.data.school.School;
import gs.data.test.*;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.test.rating.*;
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
@RequestMapping("/school/profileRatingsSubgroups.page")
public class SchoolProfileRatingsSubgroupsController extends AbstractSchoolProfileController {
    private static final Log _log = LogFactory.getLog(SchoolProfileRatingsController.class);
    public static final String VIEW = "school/profileRatingsSubgroups";

    @Autowired
    private IRatingsConfigDao _ratingsConfigDao;

    @Autowired
    private ITestDataSetDao _testDataSetDao;

    @Autowired
    private TestManager _testManager;

    @Autowired
    private SchoolProfileRatingsHelper _schoolProfileRatingsHelper;

    private boolean _showingSubjectGroups = false;

    //===================== REQUEST HANDLERS =======================

    @RequestMapping(method=RequestMethod.GET)
    public String showRatingsPage(ModelMap modelMap,
                                  HttpServletRequest request) throws Exception {

        School school = getSchool(request);
        modelMap.put("school", school);

        modelMap.addAllAttributes(getData(school, request));

        return VIEW;
    }

    //===================== Data ===================================

    public Map<String,Object> getData(School school, HttpServletRequest request) throws Exception {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        RatingsCommand ratingsCommand = new RatingsCommand();
        ratingsCommand.setSchool(school);
        ratingsCommand.setState(school.getDatabaseState());

        _schoolProfileRatingsHelper.populateRatingsCommandWithData(request, ratingsCommand, _showingSubjectGroups);

        dataMap.put("ratingsCmd", ratingsCommand);
        return dataMap;
    }
}