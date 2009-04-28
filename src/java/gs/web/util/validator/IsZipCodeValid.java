package gs.web.util.validator;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;

import gs.data.geo.bestplaces.BpZip;
import gs.data.geo.IGeoDao;
import gs.data.state.State;

/**
 * @author npatury <mailto:npatury@greatschools.net>
 */
public class IsZipCodeValid implements Controller {

    private IGeoDao _geoDao;

    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        String zipcode = request.getParameter("zipcode");
        if (StringUtils.isNotBlank(zipcode)) {
            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();
            BpZip bpZip = getGeoDao().findZip(zipcode);
            State zipState = null;
            if (bpZip != null) {
                zipState = bpZip.getState();
            }
            if (zipState != null) {
                out.write("true");
            } else {
                out.write("false");
            }
        }
        return null;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }
}
