package gs.web.test;

/**
 * Created with IntelliJ IDEA.
 * User: eddie
 * Date: 4/8/13
 * Time: 4:04 PM
 * To change this template use File | Settings | File Templates.
 */

import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.school.LevelCode;
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
import java.util.List;


/**
 * The required request parameters are:
 *     state: a 2-letter abbreviation
 *     city: the canonical city name
 *
 */
public class DistrictsInCityAjaxController implements Controller {

    protected final Logger _log = Logger.getLogger(getClass());
    public static final String BEAN_ID = "/test/districtsInCity.page";
    private IDistrictDao _districtDao;
    private static final StateManager _stateManager = new StateManager();

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String levels = request.getParameter("levels");
        LevelCode filter = null;
        if (StringUtils.isNotBlank(levels)) {
            filter = LevelCode.createLevelCode(levels);
        }
        response.setContentType("text/xml");
        PrintWriter out = response.getWriter();
        String optionsParam = request.getParameter("printOptionsOnly");
        boolean printOptionsOnly = (StringUtils.isNotBlank(optionsParam) ? Boolean.valueOf(optionsParam) : false);
        try {
            outputdistrictSelect(request, out, filter, printOptionsOnly);
        } catch (Exception e) {
            _log.warn("Error getting district list.", e);
            out.println("Error getting district list");
        }
        return null;
    }

    protected void outputdistrictSelect(HttpServletRequest request, PrintWriter out, LevelCode filter, boolean printOptionsOnly) {
        State state = _stateManager.getState(request.getParameter("state"));
        String onChange = request.getParameter("onchange");
        String city = request.getParameter("city");
        String county = request.getParameter("county");
        String includePrivatedistrictsStr = request.getParameter("includePrivatedistricts");
        boolean includePrivatedistricts = (StringUtils.isNotBlank(includePrivatedistrictsStr) ? Boolean.valueOf(includePrivatedistrictsStr) : false);
        String choosedistrictLabel = request.getParameter("choosedistrictLabel");
        if (StringUtils.isBlank(choosedistrictLabel)) {
            choosedistrictLabel = "Choose district";
        }
        List<District> districts = null;
        if(county != null){
            districts = _districtDao.findDistrictsInCounty(state, county,false);
        }else if(city != null){
            districts = _districtDao.findDistrictsInCity(state, city);
        }
        String selected = districts != null && districts.size() == 1 ? "selected" : "";
        if(districts == null || districts.size() == 0){
            out.print("no districts");
            return;
        }
        if (!printOptionsOnly) {
            out.println("<select id=\"districtSelect\" name=\"sid\" class=\"selectdistrict\""+(StringUtils.isNotBlank(onChange) ? " onchange=\"" + onChange + "\"" : "") +  ">");
        }
        out.println("<option value=\"\">" + choosedistrictLabel + "</option>");
        for (District district : districts) {
                if (filter != null) {
                    if (!filter.containsSimilarLevelCode(district.getLevelCode())) {
                        continue;
                    }
                }
                out.print("<option value=\"" + district.getId() + "\"" + " " + selected + ">");
                //out.print("<option value=\"" + district.getId() + "\" \"" + selected + ">");
                out.print(StringEscapeUtils.escapeHtml(district.getName()));
                out.println("</option>");
        }
        if (!printOptionsOnly) {
            out.print("</select>");
        }
    }

//    protected void outputdistrictOptions(HttpServletRequest request, PrintWriter out, LevelCode filter) {
//        State state = _stateManager.getState(request.getParameter("state"));
//        String city = request.getParameter("city");
//        String includePrivatedistrictsStr = request.getParameter("includePrivatedistricts");
//        boolean includePrivatedistricts = (StringUtils.isNotBlank(includePrivatedistrictsStr) ? Boolean.valueOf(includePrivatedistrictsStr) : false);
//        String choosedistrictLabel = request.getParameter("choosedistrictLabel");
//        if (StringUtils.isBlank(choosedistrictLabel)) {
//            choosedistrictLabel = "2. Choose district";
//        }
//        List<district> districts = _districtDao.finddistrictsInCity(state, city, false);
//        out.println("<option value=\"\">" + choosedistrictLabel + "</option>");
//        for (district district : districts) {
//            if (includePrivatedistricts || district.getType() != districtType.PRIVATE) {
//                if (filter != null) {
//                    if (!filter.containsSimilarLevelCode(district.getLevelCode())) {
//                        continue;
//                    }
//                }
//                out.print("<option value=\"" + district.getId() + "\">");
//                out.print(StringEscapeUtils.escapeHtml(district.getName()));
//                out.println("</option>");
//            }
//        }
//    }

    public IDistrictDao getDistrictDao() {
        return _districtDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
    }
}