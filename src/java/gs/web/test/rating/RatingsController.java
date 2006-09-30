/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: RatingsController.java,v 1.6 2006/09/30 06:52:04 apeterson Exp $
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Draw the rating page
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class RatingsController extends SimpleFormController {

    private static final Log _log = LogFactory.getLog(RatingsController.class);

    private String _viewName;
    private ISchoolDao _schoolDao;
    private IRatingsConfigDao _ratingsConfigDao;
    private ITestDataSetDao _testDataSetDao;
    private List _onLoadValidators;
    private TestManager _testManager;

    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {
        RatingsCommand ratingsCommand = (RatingsCommand) command;
        List validators = getOnLoadValidators();

        for (Iterator iter = validators.iterator(); iter.hasNext();) {
            Validator val = (Validator) iter.next();
            if (val.supports(ratingsCommand.getClass())) {
                val.validate(ratingsCommand, errors);
            }
        }

        if (!errors.hasErrors()) {
            State state = ratingsCommand.getState();
            try {
                School s = getSchoolDao().getSchoolById(state, new Integer(ratingsCommand.getSchoolId()));
                ratingsCommand.setSchool(s);
            } catch (ObjectRetrievalFailureException e) {
                errors.reject("nokey", "Invalid school");
                _log.info("Invalid school passed" + state.getAbbreviation() + ratingsCommand.getSchoolId());
            }
        }
    }

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        Map model = new HashMap();

        if (!errors.hasErrors()) {
            RatingsCommand ratingsCommand = (RatingsCommand) command;
            IRatingsConfig ratingsConfig = _ratingsConfigDao.restoreRatingsConfig(ratingsCommand.getState());

            if (null != ratingsConfig) {
                SchoolRatingsDisplay ratingsDisplay =
                        new SchoolRatingsDisplay(ratingsConfig, ratingsCommand.getSchool(), _testDataSetDao);
                if (ratingsCommand.isShowingSubjects()) {
                    ratingsCommand.setRatingsDisplay(ratingsDisplay);
                }   else {
                    OverallRatingDecorator ratingDecorator = new OverallRatingDecorator(ratingsDisplay);
                    ratingsCommand.setRatingsDisplay(ratingDecorator);
                }

                SchoolTestValue schoolTestValue =
                        getTestManager().getOverallRating(ratingsCommand.getSchool(), ratingsConfig.getYear());
                ratingsCommand.setOverallRating(schoolTestValue.getValueInteger());
            }
        }
        model.put(getCommandName(), command);
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
}