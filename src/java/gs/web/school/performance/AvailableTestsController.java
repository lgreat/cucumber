/*
 * Copyright (c) 2005 NDP Software. All Rights Reserved.
 * $Id: AvailableTestsController.java,v 1.2 2006/09/08 23:23:34 apeterson Exp $
 */

package gs.web.school.performance;

import gs.data.school.Grade;
import gs.data.state.State;
import gs.data.test.*;
import gs.web.util.context.ISessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides...
 *
 * @author <a href="mailto:ndp@mac.com">Andrew J. Peterson</a>
 */
public final class AvailableTestsController extends ParameterizableViewController {

    private ITestDataSetDao _testDataSetDao;
    private ISubjectDao _subjectDao;
    private ITestDataTypeDao _testDataTypeDao;

    protected Log _log = LogFactory.getLog(AvailableTestsController.class);

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {
        ModelAndView modelAndView = super.handleRequestInternal(request, httpServletResponse);

        ISessionContext sessionContext = SessionContextUtil.getSessionContext(request);

        String yearStr = request.getParameter("year");
        int year = Integer.parseInt(yearStr);
        modelAndView.addObject("year", new Integer(year));
        modelAndView.addObject("yearStr", "" + (year-1) + "-" + year);

        State state = sessionContext.getState();


        List subjects = _testDataSetDao.findSubjects(state, year);

        String[] subjectNames = new String[subjects.size()];
        for (java.util.ListIterator iter = subjects.listIterator(); iter.hasNext();) {
            final Subject sub = (Subject) iter.next();
            final String name = _subjectDao.findSubjectName(sub, state);
            subjectNames[iter.nextIndex() - 1] = name + " (" + sub.getSubjectId() + ")";
        }
        modelAndView.addObject("subjectNames", subjectNames);


        List dataTypeIds = _testDataSetDao.findDataTypeIds(state, year);
        List dataTypes = new ArrayList();
        for (Iterator iter = dataTypeIds.iterator(); iter.hasNext();) {
            Integer id = (Integer) iter.next();
            dataTypes.add(_testDataTypeDao.getDataType(id.intValue()));
        }
        modelAndView.addObject("dataTypes", dataTypes);

        List testInfo = new ArrayList();
        for (Iterator iter = Grade.iterator(); iter.hasNext();) {
            final Grade grade = (Grade) iter.next();
            final List list = _testDataSetDao.findDataSets(state, year, null, null, grade, new Integer(1), ITestDataSetDao.UNSPECIFIED_PROFICIENCY_BAND, true);
            _log.error("Grade="+grade+" count="+list.size());
            if (list.size() > 0) {
                testInfo.add(new GradeInfo(grade, list));
            }
        }
        modelAndView.addObject("testInfo", testInfo);


        return modelAndView;
    }

    public void setTestDataSetDao(ITestDataSetDao testDataSetDao) {
        _testDataSetDao = testDataSetDao;
    }

    public void setSubjectDao(ISubjectDao subjectDao) {
        _subjectDao = subjectDao;
    }

    public void setTestDataTypeDao(ITestDataTypeDao testDataTypeDao) {
        _testDataTypeDao = testDataTypeDao;
    }

    public class GradeInfo extends Object {
        private final Grade _grade;
        private final List _list;


        public GradeInfo(Grade grade, List list) {
            _grade = grade;
            _list = list;
        }

        public String getGrade() {
            return _grade.getName();
        }

        public List getTestDataSets() {
            return _list;
        }

    }
}
