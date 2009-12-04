package gs.web.jsp;

import gs.data.content.IArticleDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import org.apache.log4j.Logger;

/**
 * This abstract class provides access to varios DAOs.
 *
 * @author greatschools.org>
 */
public abstract class BaseTagHandler extends SpringTagHandler {

    protected static final Logger _log = Logger.getLogger(BaseTagHandler.class);
    private static ISchoolDao _schoolDao;
    private static IArticleDao _articleDao;
    private static IDistrictDao _districtDao;
    private IReviewDao _reviewDao;

    protected String _query = "";

    protected ISchoolDao getSchoolDao() {
        if (_schoolDao == null) {
            _schoolDao = (ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID);
        }
        return _schoolDao;
    }

    protected IArticleDao getArticleDao() {
        if (_articleDao == null) {
            _articleDao = (IArticleDao) getApplicationContext().getBean(IArticleDao.BEAN_ID);
        }
        return _articleDao;
    }

    /**
     * Returns a <code>School</code> obeject from the provided state.  If either
     * the state or id parameters are null, null will be returned.  Excepections
     * throws by the schoolDao are swallowed and logged in this method.
     *
     * @param state a State type
     * @param id the database id of the school
     * @return a <code>School</code> object or null
     */
    protected School getSchool(State state, Integer id) {
        School school = null;
        if (state != null && id != null) {
            try {
                school = getSchoolDao().getSchoolById(state, id);
            } catch (Exception e) {
                _log.warn("error retrieving school: ", e);
            }
        }
        return school;
    }

    protected IDistrictDao getDistrictDao() {
        if (_districtDao == null) {
            _districtDao = (IDistrictDao) getApplicationContext().getBean(IDistrictDao.BEAN_ID);
        }
        return _districtDao;
    }

    protected IReviewDao getReviewDao() {
        if (_reviewDao == null){
            _reviewDao = (IReviewDao) getApplicationContext().getBean(IReviewDao.BEAN_ID);
        }
        return _reviewDao;
    }

    protected String escapeLongstate(String title) {
        String stateString = getStateOrDefault().getLongName();
        String outString = title.replace('$', ' ');
        return outString.replaceAll("LONGSTATE", stateString);
    }

    /**
     * @return The current <code>State</code> based on knowledge of location
     *         awareness in the <code>SessionConetext</code> object, or CA if there
     *         is no current location awareness.
     */
    protected State getState() {
        SessionContext sc = getSessionContext();
        State state = State.CA;
        if (sc != null) {
            state = sc.getStateOrDefault();
        }
        return state;
    }

    /**
     * Another convenience method to get the hostname.
     *
     * @return <code>String</code>
     */
    protected String getHostname() {
        SessionContext sc = getSessionContext();
        if (sc != null) {
            return sc.getHostName();
        }
        return null;
    }

    /**
     * @return a non-null <code>State</code> object.
     */
    protected State getStateOrDefault() {
        State s = getState();
        if (s == null) {
            s = State.CA;
        }
        return s;
    }
}
