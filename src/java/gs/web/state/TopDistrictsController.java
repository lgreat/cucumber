/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: TopDistrictsController.java,v 1.7 2005/11/01 21:10:52 apeterson Exp $
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
    private String _urlTemplate = "/modperl/browse_district/$DISTRICT/$STATE";
    //                            "/cgi-bin/$STATE/district_profile/$DISTRICT"


    /**
     * Handle a request for the top districts ina given state.
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ISessionFacade context = SessionFacade.getInstance(request);
        request.getParameter(SessionContextUtil.STATE_PARAM);

        State state = context.getStateOrDefault();

        Map model = new HashMap();

        model.put("header", state.getLongName() + " Districts");

        Integer[] districtIds = state.getTopDistricts();
        List items = new ArrayList(districtIds.length);
        for (int i = 0; i < districtIds.length; i++) {
            /*
                There's some extra error handling code here specifically designed
                to get us through testing/development environments. If the given
                district (or even the whole district table) is not available,
                then the code here catches the exception and proceeds, building
                a fake name for the district.
                If this is a real district, we will see references to it in
                the 404 report-- but there may be a better way to fix this.
            */
            String url = _urlTemplate;
            url = url.replaceAll("\\$STATE", state.getAbbreviationLowerCase());
            url = url.replaceAll("\\$DISTRICT", districtIds[i].toString());
            String name;
            try {
                District district = _districtDao.findDistrictById(state, districtIds[i]);
                name = district.getName();
            } catch (Exception e) {
                name = state.getAbbreviation() + " District " + "ABCDEFG".substring(i, i + 1);
            }
            Anchor anchor = new Anchor(url, name);
            items.add(anchor);
        }
        items.add(new Anchor("/modperl/distlist/" + state.getAbbreviation(),
                "View all " + state.getLongName() + " districts",
                "viewall"));
        model.put("results", items);

        ModelAndView modelAndView = new ModelAndView(_viewName, model);
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
