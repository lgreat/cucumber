/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ParentPollController.java,v 1.1 2005/12/07 17:45:45 apeterson Exp $
 */

package gs.web.content;

import gs.data.admin.IPropertyDao;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class ParentPollController extends AbstractController {


    private String _viewName;
    private IPropertyDao _propertyDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map model = new HashMap();

        String parentPollId = _propertyDao.getProperty("parentPollId");
        model.put("parentPollId", parentPollId);

        return new ModelAndView(_viewName, model);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public IPropertyDao getPropertyDao() {
        return _propertyDao;
    }

    public void setPropertyDao(IPropertyDao propertyDao) {
        _propertyDao = propertyDao;
    }


}
