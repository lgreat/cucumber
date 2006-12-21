/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: RatingsController.java,v 1.16 2006/12/21 19:58:19 thuss Exp $
 */
package gs.web.test.rating;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.ITestDataSetDao;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.util.PageHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Draw the rating page
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class RatingsController extends AbstractCommandController {

    private static final Log _log = LogFactory.getLog(RatingsController.class);

    private String _viewName;
    private ISchoolDao _schoolDao;
    private IRatingsConfigDao _ratingsConfigDao;
    private ITestDataSetDao _testDataSetDao;
    private List _onLoadValidators;
    private TestManager _testManager;
    private boolean _showingSubjectGroups = false;

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
            return new ModelAndView(getViewName(), referenceData(request, command, errors));
        } else {
            return new ModelAndView(getViewName(), errors.getModel());
        }
    }

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        Map model = new HashMap();
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
                    //TODO turn back on once we support ratings for previous years
                    SchoolTestValue previousYearOverallRating = null;
                            //getTestManager().getOverallRating(ratingsCommand.getSchool(), ratingsConfig.getYear() - 1);

                    Integer currentRating = schoolTestValue.getValueInteger();
                    Integer previousRating = null;

                    if (previousYearOverallRating != null && previousYearOverallRating.getValueInteger() != null) {
                        previousRating = previousYearOverallRating.getValueInteger();
                    }

                    IRatingsDisplay.IRowGroup.IRow.ICell overallRatingCell =
                            new Cell(schoolTestValue.getValueInteger(),
                                    TestManager.calculateRatingTrend(currentRating, previousRating));
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
}