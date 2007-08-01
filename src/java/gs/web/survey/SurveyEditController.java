package gs.web.survey;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.lang.StringUtils;
import gs.web.util.ReadWriteController;
import gs.data.survey.ISurveyDao;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SurveyEditController extends SimpleFormController
        implements ReadWriteController {

    public static final String BEAN_ID = "/survey/edit.page";

    private ISurveyDao _surveyDao;

    /**
     * If a new Survey is being created, then a new blank Survey is returned, otherwise
     * an existing Survey is summoned from the SurveyDao.
     * @param request an HttpServletRequest object
     * @return the command object: a <code>Survey</code>
     * @throws Exception
     */
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        String id = (String)request.getParameter("id");
        if (StringUtils.isNotBlank(id)) {
            return _surveyDao.getSurveyById(Integer.valueOf(id));
        }
        return createCommand();
	}

    protected ModelAndView onSubmit(Object cmd, BindException errors) throws Exception {
        System.out.println ("cmd: " + cmd);
        return super.onSubmit(cmd, errors);
    }

    public ISurveyDao getSurveyDao() {
        return _surveyDao;
    }

    public void setSurveyDao(ISurveyDao surveyDao) {
        _surveyDao = surveyDao;
    }
}
