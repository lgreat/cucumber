package gs.web.test;

import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.*;
import gs.data.school.census.*;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: eddie
 * Date: 4/9/13
 * Time: 3:16 PM
 * To change this template use File | Settings | File Templates.
 */



public class EntityInfoAjaxController implements Controller {

    protected final Logger _log = Logger.getLogger(getClass());
    public static final String BEAN_ID = "/test/entityInfo.page";
    private ISchoolDao _schoolDao;
    private IDistrictDao _districtDao;
    private ICensusInfo _censusInfo;

    private IEspResponseDao _espResponseDao;
    private static final StateManager _stateManager = new StateManager();

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String optionsParam = request.getParameter("printOptionsOnly");
        boolean printOptionsOnly = (StringUtils.isNotBlank(optionsParam) ? Boolean.valueOf(optionsParam) : false);
        try {
            outputInfo(request, out);
        } catch (Exception e) {
            _log.warn("Error getting school info.", e);
            out.println("Error getting school info");
        }
        return null;
    }

    protected void outputInfo(HttpServletRequest request, PrintWriter out) {
        State state = _stateManager.getState(request.getParameter("state"));
        String id = request.getParameter("id");
        String entityType = request.getParameter("entityType");

        JSONObject json = new JSONObject();

        if(entityType.equals("school")){
            School school = _schoolDao.getSchoolById(state, new Integer(id));
            if(school != null){

                Integer lowageCensusYear = 0;
                String censusLowAge = "";

                String grades = "";
                if(school.getGradeLevels() != null){
                    grades = school.getGradeLevels().getCommaSeparatedString();
                }

                String schoolType = "";
                if(school.getType() != null){
                    schoolType = school.getType().getSchoolTypeName();
                }

                enterJson(json,"districtId",integerToString(school.getDistrictId()));
                enterJson(json,"name",school.getName());

                enterJson(json,"grades",grades);
                enterJson(json,"schoolType",schoolType);
                enterJson(json,"street",school.getStreet());
                enterJson(json,"streetLine2",school.getStreetLine2());
                enterJson(json,"city",school.getCity());
                enterJson(json,"Zipcode",school.getZipcode());


                //do county dropdown
                enterJson(json,"county",school.getCounty());

                enterJson(json,"enrollment",integerToString(school.getEnrollment()));

                //show capacity for pk-only schools
                String capacity = "";
                if(school.getLevelCode().equals(LevelCode.PRESCHOOL)){
                    capacity = integerToString(school.getEnrollmentOrCapacity());
                    enterJson(json,"enrollment",capacity);
                }

                enterJson(json,"phone",school.getPhone());
                enterJson(json,"fax",school.getFax());
                enterJson(json,"webSite",school.getWebSite());
                enterJson(json,"affiliation",school.getAffiliation());
                enterJson(json,"association",school.getAssociation());


                String subtypes = "";

                if(school.getSubtype() != null){
                    subtypes = school.getSubtype().getCommaSeparatedString();
                }

                String gender = "";
                StringUtils.contains(subtypes,"coed");
                if(StringUtils.contains(subtypes,"coed")){
                    gender = "coed";
                }
                else if(StringUtils.contains(subtypes,"all_male")){
                    gender = "all_male";
                }
                else if(StringUtils.contains(subtypes,"all_female")){
                    gender = "all_female";
                }
                enterJson(json,"gender",gender);

                String preschoolSubtype = "";
                if(school.getPreschoolSubtype() != null){
                    preschoolSubtype =  school.getPreschoolSubtype().getCommaSeparatedString();
                }
                enterJson(json,"preschoolSubtype",preschoolSubtype);


                if(school.getCensusInfo() != null){
                    SchoolCensusValue scv = school.getCensusInfo().getLatestValue(school, CensusDataType.HEAD_OFFICIAL_NAME);
                    enterJson(json,"headOfficialName",censusValueToString(scv));

                    scv = school.getCensusInfo().getLatestValue(school, CensusDataType.HEAD_OFFICIAL_EMAIL);
                    enterJson(json,"headOfficialEmail",censusValueToString(scv));


                    scv = school.getCensusInfo().getLatestValue(school, CensusDataType.LOW_AGE);
                    if(scv != null ){
                        lowageCensusYear = scv.getDataSet().getYear();
                        censusLowAge = scv.getValueText();
                    }
                    enterJson(json,"lowAge",censusValueToString(scv));

                    scv = school.getCensusInfo().getLatestValue(school, CensusDataType.HIGH_AGE);
                    enterJson(json,"highAge",censusValueToString(scv));

                    scv = school.getCensusInfo().getLatestValue(school, CensusDataType.BILINGUAL_INTRUCTION_OFFERED);
                    enterJson(json,"bilingual",censusYNValueToString(scv));

                    scv = school.getCensusInfo().getLatestValue(school, CensusDataType.SPECIAL_ED_OFFERED);
                    enterJson(json,"specialEd",censusYNValueToString(scv));

                    scv = school.getCensusInfo().getLatestValue(school, CensusDataType.COMPUTERS_AVAILABLE);
                    enterJson(json,"computers",censusYNValueToString(scv));

                    scv = school.getCensusInfo().getLatestValue(school, CensusDataType.BEFORE_AFTER_SUPERVISION);
                    enterJson(json,"extendedCare",censusYNValueToString(scv));

                }




                Map<String,String> espVarMap = new HashMap();

                espVarMap.put("start_time","startTime") ;
                espVarMap.put("end_time","endTime") ;
                espVarMap.put("age_pk_start","lowAge") ;

                List<EspResponse> responses = _espResponseDao.getResponsesByKeys(school,espVarMap.keySet());
                for (String key : espVarMap.keySet()) {
                    enterJson(json,espVarMap.get(key),"");
                }

                Iterator<EspResponse> it = responses.iterator();
                while(it.hasNext())
                {
                    Object obj = it.next();
                    EspResponse response = (EspResponse) obj;
                    String keyName = espVarMap.get(response.getKey());
                    if(response.getKey().equals("age_pk_start")){
                        Date date = response.getCreated();
                        Calendar cal = Calendar.getInstance();
                        cal.set(lowageCensusYear.intValue()-1,Calendar.SEPTEMBER,30);
                        if(date.after(cal.getTime())){
                            enterJson(json,keyName,response.getValue());
                        }else{
                            enterJson(json,keyName,censusLowAge);
                        }
                    }else{
                        enterJson(json,keyName,response.getValue());
                    }
                }
                out.print(json.toString());
            }
        }

        if(entityType.equals("district")){
            District district = _districtDao.findDistrictById(state, new Integer(id));
            if(district != null){
                String grades = "";
                if(district.getGradeLevels() != null){
                    grades = district.getGradeLevels().getCommaSeparatedString();
                }

                enterJson(json,"name",district.getName());

                enterJson(json,"grades",grades);
                enterJson(json,"street",district.getPhysicalAddress().getStreet());
                enterJson(json,"streetLine2",district.getPhysicalAddress().getStreetLine2());
                enterJson(json,"city",district.getPhysicalAddress().getCity());
                enterJson(json,"Zipcode",district.getPhysicalAddress().getZip());


                //do county dropdown
                enterJson(json,"county",district.getCounty());

                enterJson(json,"phone",district.getPhone());
                enterJson(json,"fax",district.getFax());
                enterJson(json,"webSite",district.getWebSite());



                DistrictCensusValue scv = getCensusInfo().getLatestValue(district, CensusDataType.HEAD_OFFICIAL_NAME);
                enterJson(json,"headOfficialName",censusValueToString(scv));
                scv = getCensusInfo().getLatestValue(district, CensusDataType.HEAD_OFFICIAL_EMAIL);
                enterJson(json,"headOfficialEmail",censusValueToString(scv));


                /*
                if(district.getc != null){
                    SchoolCensusValue scv = school.getCensusInfo().getLatestValue(school, CensusDataType.HEAD_OFFICIAL_NAME);
                    enterJson(json,"headOfficialName",censusValueToString(scv));

                    scv = school.getCensusInfo().getLatestValue(school, CensusDataType.HEAD_OFFICIAL_EMAIL);
                    enterJson(json,"headOfficialEmail",censusValueToString(scv));
                 */

                out.print(json.toString());
            }
        }
    }

    private ICensusDataSetDao _censusDataSetDao;
    public ICensusDataSetDao getCensusDataSetDao() {
        return _censusDataSetDao;
    }

    public void setCensusDataSetDao(ICensusDataSetDao censusDataSetDao) {
        _censusDataSetDao = censusDataSetDao;
    }

    ICensusInfo getCensusInfo() {
        if (_censusInfo == null) {
            CensusInfoFactory censusInfoFactory = new CensusInfoFactory(getCensusDataSetDao());
            _censusInfo = censusInfoFactory.getNbcCensusInfo();
        }
        return _censusInfo;
    }

    private String integerToString (Integer intValue){
        if(intValue != null){
            return intValue.toString();
        }
        return "";
    }
    private void enterJson(JSONObject jo,String keyName,String valueName){
        if(valueName == null){
            valueName = "";
        }
        try{
            jo.put(keyName,valueName);
        }catch(Exception e){

        }
    }
    private String censusValueToString(SchoolCensusValue scv){
        if(scv != null ){
            return scv.getValueText();
        }
        return "";
    }

    private String censusValueToString(DistrictCensusValue scv){
        if(scv != null ){
            return scv.getValueText();
        }
        return "";
    }

    private String censusYNValueToString(SchoolCensusValue scv){
        if(scv != null ){
            if(scv.getValueText().equals("Y")){
                return "Yes";
            }
            if(scv.getValueText().equals("N")){
                return "No";
            }
            return "";
        }
        return "";
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IDistrictDao getDistrictDao() {
        return _districtDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
    }

    public IEspResponseDao getEspResponseDao() {
        return _espResponseDao;
    }

    public void setEspResponseDao(IEspResponseDao espResponseDao) {
        _espResponseDao = espResponseDao;
    }


}