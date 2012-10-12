/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: RatingsController.java,v 1.27 2012/10/12 01:53:45 ssprouse Exp $
 */
package gs.web.test.rating;

import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.ITestDataSetDao;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import gs.web.request.RequestInfo;
import gs.web.school.AbstractSchoolController;
import gs.web.school.SchoolProfileHeaderHelper;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Draw the rating page
 *
 * @author David Lee <mailto:dlee@greatschools.org>
 */
public class RatingsController extends AbstractCommandController implements IControllerFamilySpecifier {

    private static final Log _log = LogFactory.getLog(RatingsController.class);

    private String _viewName;
    private ISchoolDao _schoolDao;
    private IRatingsConfigDao _ratingsConfigDao;
    private ITestDataSetDao _testDataSetDao;
    private List _onLoadValidators;
    private TestManager _testManager;
    private SchoolProfileHeaderHelper _schoolProfileHeaderHelper;
    private boolean _showingSubjectGroups = false;
    private ControllerFamily _controllerFamily;

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        RatingsCommand ratingsCommand = (RatingsCommand) command;
        List validators = getOnLoadValidators();

        for (Iterator iter = validators.iterator(); iter.hasNext();) {
            Validator val = (Validator) iter.next();
            if (val.supports(ratingsCommand.getClass())) {
                val.validate(ratingsCommand, errors);
            }
        }

        //populate school
        if (!errors.hasErrors()) {
            State state = ratingsCommand.getState();
            try {
                School s = getSchoolDao().getSchoolById(state, new Integer(ratingsCommand.getSchoolId()));
                if (s.isActive()) {
                    
                    // Preschool profile pages should be hosted from pk.greatschools.org (GS-12127). Redirect if needed
                    if (LevelCode.PRESCHOOL.equals(s.getLevelCode())) {
                        RequestInfo hostnameInfo = (RequestInfo) request.getAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME);
                        if (!hostnameInfo.isOnPkSubdomain() && hostnameInfo.isPkSubdomainSupported()) {
                            UrlBuilder urlBuilder = new UrlBuilder(s, UrlBuilder.SCHOOL_PROFILE_RATINGS);
                            return new ModelAndView(new RedirectView301(urlBuilder.asFullUrl(request)));
                        }
                    }

                    // GS-13082 Redirect to new profile if eligible
                    if (AbstractSchoolController.shouldRedirectToNewProfile(s)) {
                        return AbstractSchoolController.getRedirectToNewProfileModelAndView
                                (s, request, AbstractSchoolController.NewProfileTabs.ratings);
                    }


                    ratingsCommand.setSchool(s);
                } else {
                    errors.reject("nokey", "School is no longer active.");
                }
            } catch (ObjectRetrievalFailureException e) {
                errors.reject("nokey", "Invalid school");
                _log.info("Invalid school passed" + state.getAbbreviation() + ratingsCommand.getSchoolId());
            }
        }

        //another check for errors since retrieving school could have set errors
        if (!errors.hasErrors()) {
            Map<String, Object> model = referenceData(request, command, errors);
            _schoolProfileHeaderHelper.updateModel(request, response, ratingsCommand.getSchool(), model);
            return new ModelAndView(getViewName(), model);
        } else {
            return new ModelAndView(getViewName(), errors.getModel());
        }
    }

    protected Map<String,Object> referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        Map<String,Object> model = new HashMap<String, Object>();
        RatingsCommand ratingsCommand = (RatingsCommand) command;

        if (!errors.hasErrors()) {

            PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
            boolean isFromCache = true;

            if (null != pageHelper && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer()) {
                isFromCache = false;
            }
            IRatingsConfig ratingsConfig = _ratingsConfigDao.restoreRatingsConfig(ratingsCommand.getState(), isFromCache);

            if (null != ratingsConfig) {
                ratingsCommand.setRatingYear(ratingsConfig.getYear());

                SchoolTestValue schoolTestValue =
                        getTestManager().getOverallRating(ratingsCommand.getSchool(), ratingsConfig.getYear());

                if (null != schoolTestValue && null != schoolTestValue.getValueInteger()) {
                    IRatingsDisplay.IRowGroup.IRow.ICell overallRatingCell = new Cell(schoolTestValue.getValueInteger());
                    ratingsCommand.setOverallRating(overallRatingCell);

                    SchoolRatingsDisplay ratingsDisplay =
                            new SchoolRatingsDisplay(ratingsConfig, ratingsCommand.getSchool(), _testDataSetDao);

                    if (_showingSubjectGroups) {
                        ratingsCommand.setRatingsDisplay(ratingsDisplay);
                    } else {
                        OverallRatingDecorator ratingDecorator = new OverallRatingDecorator(ratingsDisplay);
                        ratingsCommand.setRatingsDisplay(ratingDecorator);
                    }
                }
            }
        }

        model.put(getCommandName(), ratingsCommand);
        return model;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public void setRatingsConfigDao(final IRatingsConfigDao ratingsConfigDao) {
        _ratingsConfigDao = ratingsConfigDao;
    }

    public void setTestDataSetDao(final ITestDataSetDao testDataSetDao) {
        _testDataSetDao = testDataSetDao;
    }

    public List getOnLoadValidators() {
        return _onLoadValidators;
    }

    public void setOnLoadValidators(List onLoadValidators) {
        _onLoadValidators = onLoadValidators;
    }

    public TestManager getTestManager() {
        return _testManager;
    }

    public void setTestManager(TestManager testManager) {
        _testManager = testManager;
    }

    public void setShowingSubjectGroups(final boolean showingSubjectGroups) {
        _showingSubjectGroups = showingSubjectGroups;
    }

    public SchoolProfileHeaderHelper getSchoolProfileHeaderHelper() {
        return _schoolProfileHeaderHelper;
    }

    public void setSchoolProfileHeaderHelper(SchoolProfileHeaderHelper schoolProfileHeaderHelper) {
        _schoolProfileHeaderHelper = schoolProfileHeaderHelper;
    }

    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
    }

    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }
}