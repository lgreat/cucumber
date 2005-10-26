/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: TopDistrictsController.java,v 1.1 2005/10/26 20:51:33 apeterson Exp $
 */

package gs.web.state;

import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.SessionContextUtil;
import gs.web.SessionFacade;
import gs.web.util.Anchor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class TopDistrictsController extends AbstractController {
    private String _viewName;
    private IDistrictDao _districtDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ISessionFacade context = SessionFacade.getInstance(request);
        request.getParameter(SessionContextUtil.STATE_PARAM);

        State state = context.getStateOrDefault();

        Map map = new HashMap();

        map.put("header", state.getLongName() + " Districts");

        Integer[] districtIds = state.getTopDistricts();
        List items = new ArrayList(districtIds.length);
        for (int i = 0; i < districtIds.length; i++) {
            District district = _districtDao.findDistrictById(state, districtIds[i]);
            Anchor anchor = new Anchor("/cgi-bin/" + state.getAbbreviationLowerCase() +
                    "/district_profile/" + district.getId(),
                    district.getName());
            items.add(anchor);
        }
        items.add(new Anchor("/modperl/distlist/" + state.getAbbreviation(),
                "View all " + state.getLongName() + " districts",
                "viewall"));
        map.put("results", items);

        ModelAndView modelAndView = new ModelAndView(_viewName, map);
        return modelAndView;
    }


    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public IDistrictDao getDistrictDao() {
        return _districtDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
    }
}
