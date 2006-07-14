package gs.web.school;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.web.util.context.ISessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.jsp.Util;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.charter.ICharterSchoolInfo;
import gs.data.school.census.ICharterSchoolInfoDao;
import gs.data.state.State;

import java.util.Map;

/**
 * This controller manages requests to autherizers.page for charter school
 * authorizer supplemental info.  The following query parameters are required:
 * <ul>
 * <li>state  : the school's database state</li>
 * <li>school : the charter school's id</li>
 * </ul>
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class AuthorizersController extends AbstractController {

    /** Spring bean ID */
    public static final String BEAN_ID = "/school/authorizers";

    /** used to retrieve the school using the id */
    private ISchoolDao _schoolDao;

    /** used to get the ICharterSchoolInfo object given a school */
    private ICharterSchoolInfoDao _charterSchoolInfoDao;

    private static final Logger _log =
            Logger.getLogger(AuthorizersController.class);

    private static final String PARAM_SCHOOL = "school";

    private static final String errorMessage = "Sorry, no school could be found";

    /**
     * In addition to passing the school object and a source string to the page,
     * this request handler gathers together <code>java.util.Map</code> objects
     * with the data for the following data groups: School Structure;
     * School Mission and Climate; Academic Evaluation; Demand.  If a group does
     * not have any data, a null <code>Map</code> should be returned.  The maps
     * returned by (within) the model retain the insertion order upon iteration
     * because they are backed by
     * <code>org.apache.commons.collections.map.ListOrderedMap</code>s.
     * @param request
     * @param response
     * @return a Map model.
     * @throws Exception
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        ModelAndView mAndV = new ModelAndView("school/authorizers");
        ISessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        String schoolId = request.getParameter(PARAM_SCHOOL);

        if (schoolId != null) {
            try {
                School school = _schoolDao.getSchoolById(sessionContext.getState(),
                        Integer.valueOf(schoolId));
                // Don't worry about a null school.  The DAO will throw an
                // Exception if it can't find a school.
                ICharterSchoolInfo info = _charterSchoolInfoDao.getInfo(school);
                mAndV.getModel().put("school", school);
                mAndV.getModel().put("structureData", getStructureData(info));
                mAndV.getModel().put("missionData", getMissionData(info));
                mAndV.getModel().put("academicData", getAcademicData(info));
                mAndV.getModel().put("demandData", getDemandData(info));
                mAndV.getModel().put("source", "Source: " + Util.unquote(info.getSource()));
                mAndV.getModel().put("report", getReport(school));
            } catch (Exception orfe) {
                // log this and just return an empty page rather than vomiting
                // a stack trace.  The page is responsible for handling this
                // gracefully.
                StringBuffer logBuffer = new StringBuffer();
                logBuffer.append("Couldn't get info for school: ");
                logBuffer.append(schoolId);
                logBuffer.append(" in State: ");
                logBuffer.append(sessionContext.getState().toString());
                _log.warn(logBuffer.toString(), orfe);
                mAndV.getModel().put("errormessage", errorMessage);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        return mAndV;
    }

    /** GS-1876 */
    private static String getReport(School school) {
        if (school != null && State.NY.equals(school.getDatabaseState())) {
            int id = school.getId().intValue();
            switch(id) {
                case 6368:
                case 6372:
                case 6377:
                case 6378:
                case 6379:
                case 6380:
                case 6382:
                case 6384:
                case 6386:
                case 6388:
                case 6389:
                case 6391:
                case 6393:
                case 6397:
                case 6399:
                case 6458:
                case 6460:
                case 7669:
                case 7673:
                    return "http://www.newyorkcharters.org/pubsReports.htm";
            }
        }
        return null;
    }

    private static Map getStructureData(ICharterSchoolInfo info) {
        Map demandData = null;
        if (info.getYearOpened() > -1) {
            if (demandData == null) { demandData = new ListOrderedMap(); }
            demandData.put("Year opened", new Integer(info.getYearOpened()));
        }

        String terms = info.getTermsOfCharter();
        if (StringUtils.isNotBlank(terms)) {
            if (demandData == null) { demandData = new ListOrderedMap(); }
            demandData.put("Terms of charter", terms);
        }

        if (StringUtils.isNotBlank(info.getRenewalStatus())) {
            if (demandData == null) { demandData = new ListOrderedMap(); }
            demandData.put("Renewal status", info.getRenewalStatus());
        }

        if (StringUtils.isNotBlank(info.getAuthorizer())) {
            if (demandData == null) { demandData = new ListOrderedMap(); }
            demandData.put("Authorizer", info.getAuthorizer());
        }

        if (StringUtils.isNotBlank(info.getOperator())) {
            if (demandData == null) { demandData = new ListOrderedMap(); }
            demandData.put("Operator", info.getOperator());
        }

        if (StringUtils.isNotBlank(info.getAdminStatus())) {
            if (demandData == null) { demandData = new ListOrderedMap(); }
            demandData.put("Board/administrative status", info.getAdminStatus());
        }

        if (StringUtils.isNotBlank(info.getFinancialStatus())) {
            if (demandData == null) { demandData = new ListOrderedMap(); }
            demandData.put("Financial status", info.getFinancialStatus());
        }
        return demandData;
    }

    private static Map getMissionData(ICharterSchoolInfo info) {
        Map missionData = null;

        if (StringUtils.isNotBlank(info.getSchoolFocus())) {
            if (missionData == null) { missionData = new ListOrderedMap(); }
            missionData.put("Mission", info.getSchoolFocus());
        }

        if (StringUtils.isNotBlank(info.getSchoolClimate())) {
            if (missionData == null) { missionData = new ListOrderedMap(); }
            missionData.put("Climate", info.getSchoolClimate());
        }

        return missionData;
    }

    private static Map getAcademicData(ICharterSchoolInfo info) {
        Map academicData = null;
        if (StringUtils.isNotBlank(info.getAcademicEvaluation())) {
            if (academicData == null) { academicData = new ListOrderedMap(); }
            academicData.put("Academic Evaluation", info.getAcademicEvaluation());
        }
        return academicData;
    }

    private static Map getDemandData(ICharterSchoolInfo info) {
        Map demandData = null;
        if (StringUtils.isNotEmpty(info.getDemand())) {
            demandData = new ListOrderedMap();
            String demand = info.getDemand();
            if (demand != null && (demand.indexOf("%") == -1)) {
                demand = demand + "%";
            }
            demandData.put("Demand", demand);
        }

        if (info.getWaitlist() != -1) {
            if (demandData == null) { demandData = new ListOrderedMap(); }
            demandData.put("Waitlist", (info.getWaitlist() == 0 ? "No" : "Yes"));
        }

        String retention = info.getRetention();
        if (StringUtils.isNotEmpty(retention)) {
            if (demandData == null) { demandData = new ListOrderedMap(); }
            if (retention.indexOf("%") == -1) {
                retention = retention + "%";
            }
            demandData.put("Return rate", retention);
        }
        return demandData;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public ICharterSchoolInfoDao getCharterSchoolInfoDao() {
        return _charterSchoolInfoDao;
    }

    public void setCharterSchoolInfoDao(ICharterSchoolInfoDao charterSchoolInfoDao) {
        _charterSchoolInfoDao = charterSchoolInfoDao;
    }
}