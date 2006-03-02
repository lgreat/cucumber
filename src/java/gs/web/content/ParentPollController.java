/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ParentPollController.java,v 1.3 2006/03/02 19:05:44 apeterson Exp $
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
 * Retrieves the current parent poll id from the database and passes it on
 * to the view.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class ParentPollController extends AbstractController {

    /**
     * The id of the parent poll, passed to the view, used to specify the
     * iframe contents.
     */
    public static final String MODEL_PARENT_POLL_ID = "parentPollId";

    private String _viewName;
    private IPropertyDao _propertyDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map model = new HashMap();

        String parentPollId = _propertyDao.getProperty(IPropertyDao.PARENT_POLL_ID);
        model.put(MODEL_PARENT_POLL_ID, parentPollId);

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
