package gs.web.test;

import gs.data.school.*;
import gs.data.school.census.CensusDataType;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private IEspResponseDao _espResponseDao;
    private static final StateManager _stateManager = new StateManager();

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text");
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


        if(entityType.equals("school")){
            School school = _schoolDao.getSchoolById(state, new Integer(id));
            List<EspResponse> responses = _espResponseDao.getResponses(school);
            Map<String,String> espStuff = new HashMap<String, String>();
            while(responses.iterator().hasNext()){
                Object o = responses.iterator().next();
                EspResponse response = (EspResponse) o;
                espStuff.put(response.getKey(),response.getValue());
            }
            if(school != null){
                out.print("{\"name\":\"" + school.getName() +"\"" );

                out.print(",\"grades\":\"" + school.getGradeLevels().getCommaSeparatedString()  +"\"" );
                out.print(",\"schoolType\":\"" + school.getType()  +"\"" );
                out.print(",\"street\":\"" + school.getStreet()  +"\"" );
                out.print(",\"street2\":\"" + school.getStreetLine2()  +"\"" );
                out.print(",\"zipcode\":\"" + school.getZipcode()  +"\"" );
                out.print(",\"county\":\"" + school.getCounty()  +"\"" );
                out.print(",\"enrollment\":\"" + school.getEnrollment()  +"\"" );
                out.print(",\"phone\":\"" + school.getPhone()  +"\"" );
                out.print(",\"fax\":\"" + school.getFax()  +"\"" );
                out.print(",\"website\":\"" + school.getWebSite()  +"\"" );
                out.print(",\"headOfficialName\":\"" + school.getCensusInfo().getManualValue(school, CensusDataType.HEAD_OFFICIAL_NAME).getValueText()  +"\"" );
                out.print(",\"headOfficialEmail\":\"" + school.getCensusInfo().getManualValue(school, CensusDataType.HEAD_OFFICIAL_EMAIL).getValueText()  +"\"" );
                out.print(",\"startTime\":\"" + espStuff.get("start_time")  +"\"" );
                /*
                out.print(",\"street2\":\"" + school.getStreetLine2()  +"\"" );
                out.print(",\"street2\":\"" + school.getStreetLine2()  +"\"" );
                out.print(",\"street2\":\"" + school.getStreetLine2()  +"\"" );
                out.print(",\"street2\":\"" + school.getStreetLine2()  +"\"" );
                out.print(",\"street2\":\"" + school.getStreetLine2()  +"\"" );
                                    */

                out.print("}" );
            }
        }

        if(entityType.equals("district")){
            District district = _districtDao.findDistrictById(state, new Integer(id));
            if(district != null){
                out.print("{\"name\":\"" + district.getName() +"\"" );

                out.print(",\"grades\":\"" + district.getGradeLevels().getCommaSeparatedString()  +"\"" );
                out.print(",\"street\":\"" + district.getPhysicalAddress().getStreet()  +"\"" );
                out.print(",\"street2\":\"" + district.getPhysicalAddress().getStreetLine2()  +"\"" );
                out.print(",\"zipcode\":\"" + district.getPhysicalAddress().getZip()  +"\"" );
                out.print(",\"zipcode\":\"" + district.getPhysicalAddress().getZip()  +"\"" );
                out.print(",\"zipcode\":\"" + district.getPhysicalAddress().getZip()  +"\"" );
                out.print(",\"zipcode\":\"" + district.getPhysicalAddress().getZip()  +"\"" );
                out.print(",\"zipcode\":\"" + district.getPhysicalAddress().getZip()  +"\"" );
                out.print(",\"zipcode\":\"" + district.getPhysicalAddress().getZip()  +"\"" );
                out.print(",\"zipcode\":\"" + district.getPhysicalAddress().getZip()  +"\"" );
                out.print(",\"zipcode\":\"" + district.getPhysicalAddress().getZip()  +"\"" );
                out.print(",\"zipcode\":\"" + district.getPhysicalAddress().getZip()  +"\"" );
                out.print(",\"zipcode\":\"" + district.getPhysicalAddress().getZip()  +"\"" );
                out.print(",\"zipcode\":\"" + district.getPhysicalAddress().getZip()  +"\"" );
                out.print(",\"zipcode\":\"" + district.getPhysicalAddress().getZip()  +"\"" );
                out.print(",\"zipcode\":\"" + district.getPhysicalAddress().getZip()  +"\"" );
                out.print(",\"zipcode\":\"" + district.getPhysicalAddress().getZip()  +"\"" );

                out.print("}" );
            }
        }
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