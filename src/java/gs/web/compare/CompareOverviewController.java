package gs.web.compare;

import gs.data.school.School;
import gs.data.school.census.CensusDataSet;
import gs.data.school.census.ICensusDataSetDao;
import gs.data.school.census.ISchoolCensusValueDao;
import gs.data.school.census.SchoolCensusValue;
import gs.data.state.State;

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
    private ISchoolCensusValueDao _schoolCensusValueDao;

    @Override
    protected void handleCompareRequest(HttpServletRequest request, HttpServletResponse response,
                                        List<ComparedSchoolBaseStruct> schools, Map<String, Object> model)
            throws IOException {
        model.put(MODEL_TAB, TAB_NAME);
        handleGSRating(request, schools);
        handleCommunityRating(schools);
        handleRecentReview(schools);
        buildDataStructure(model);
       
    }

    protected void buildDataStructure(Map<String, Object> model){
        List infoRows = new ArrayList();
        
        List<CensusStruct> row1 = new ArrayList();
        CensusStruct cs1 = new CensusStruct();
        cs1.setIsHeaderCell(true);
        cs1.setHeaderText("Ethnicity");
        cs1.setExtraInfo("This is the breakdown of ethnicities in schools.");
        row1.add(cs1);

        CensusStruct cs2 = new CensusStruct();
        Map school1Row1breakdownMap1 = new LinkedHashMap();
        school1Row1breakdownMap1.put("asian","47%");
        school1Row1breakdownMap1.put("white","47%");
        school1Row1breakdownMap1.put("hispanic","6%");
        cs2.setBreakdownMap(school1Row1breakdownMap1);
        row1.add(cs2);

        CensusStruct cs3 = new CensusStruct();
        Map school1Row1breakdownMap2 = new LinkedHashMap();
        school1Row1breakdownMap2.put("asian","40%");
        school1Row1breakdownMap2.put("white","40%");
        school1Row1breakdownMap2.put("hispanic","20%");
        cs3.setBreakdownMap(school1Row1breakdownMap2);
        row1.add(cs3);
        
        List<CensusStruct> row2 = new ArrayList();

        CensusStruct cs4 = new CensusStruct();
        cs4.setIsHeaderCell(true);
        cs4.setIsSimpleCell(true);
        cs4.setHeaderText("Students Per Teacher");
        row2.add(cs4);


        CensusStruct cs5 = new CensusStruct();
        cs5.setIsSimpleCell(true);
        cs5.setValue("18:1");
        row2.add(cs5);


        CensusStruct cs6 = new CensusStruct();
        cs6.setIsSimpleCell(true);
        cs6.setValue("7:1");
        row2.add(cs6);


        List<CensusStruct> row3 = new ArrayList();

        CensusStruct cs7 = new CensusStruct();
        cs7.setIsHeaderCell(true);
        cs7.setIsSimpleCell(true);
        cs7.setHeaderText("Average years teaching.");
        row3.add(cs7);


        CensusStruct cs8 = new CensusStruct();
        cs8.setIsSimpleCell(true);
        cs8.setValue("6");
        row3.add(cs8);

        CensusStruct cs9 = new CensusStruct();
        cs9.setIsSimpleCell(true);
        cs9.setValue(null);
        row3.add(cs9);

        infoRows.add(row1);
        infoRows.add(row2);
        infoRows.add(row3);
        model.put("infoRows",infoRows);
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

    public ISchoolCensusValueDao getSchoolCensusValueDao() {
        return _schoolCensusValueDao;
    }

    public void setSchoolCensusValueDao(ISchoolCensusValueDao schoolCensusValueDao) {
        _schoolCensusValueDao = schoolCensusValueDao;
    }
}
