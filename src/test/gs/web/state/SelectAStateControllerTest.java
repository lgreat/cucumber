/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SelectAStateControllerTest.java,v 1.1 2006/04/05 22:10:42 apeterson Exp $
 */

package gs.web.state;

import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

/**
 * Tests SelectAStateController.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class SelectAStateControllerTest extends BaseControllerTestCase {

    public void testSelectAStateController() throws Exception {
        SelectAStateController c = new SelectAStateController();
        c.setApplicationContext(getApplicationContext());
        c.setViewName("/stateLauncher");

        // Check no parameters, which should just go to the home page
        ModelAndView modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        RedirectView view = (RedirectView) modelAndView.getView();
        assertEquals("/", view.getUrl());

        // View will build links:
        // {url}CA{extraParams}
        // So we make sure URL gets set to what we want.
        // Currently extraParams isn't needed

        // We want it to add a state param, "?state="
        getRequest().setParameter("url", "/welcome.page");

        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        assertEquals("/stateLauncher", modelAndView.getViewName());
        Map model = modelAndView.getModel();
        assertEquals("/welcome.page?state=", model.get("url"));
        assertEquals("", model.get("extraParams"));
        assertEquals("", model.get("promotext"));


        getRequest().setParameter("url", "/districts.page?city=Lincoln");

        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        assertEquals("/stateLauncher", modelAndView.getViewName());
        model = modelAndView.getModel();
        assertEquals("/districts.page?city=Lincoln&amp;state=", model.get("url"));
        assertEquals("", model.get("extraParams"));
        assertEquals("", model.get("promotext"));


    }

}
