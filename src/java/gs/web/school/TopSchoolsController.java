package gs.web.school;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.RedirectView301;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.TopSchoolCategory;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.data.school.review.CategoryRating;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * @author thuss
 */
public class TopSchoolsController extends AbstractController {

    public static final String BEAN_ID = "/schools/topSchools.page";

    public static final String MODEL_NATIONAL = "national";
    public static final String MODEL_STATE_NAME = "stateName";
    public static final String MODEL_STATE_ABBREVIATION = "stateAbbreviation";
    public static final String MODEL_TOP_SCHOOLS = "topSchools";
    public static final String MODEL_ALL_STATES = "allStates";

    protected String _viewName;
    protected ISchoolDao _schoolDao;
    protected static Cache _cache;
    protected IReviewDao _reviewDao;

    static {
        // Cache active top schools for 6 hrs for performance
        CacheManager manager = CacheManager.create();
        _cache = new Cache(TopSchoolsController.class.getName(), 51, false, false, 3600, 600);
        manager.addCache(_cache); // You have to add a cache to a manager for it to work
    }

    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        State state = context.getState();

        // If someone accidentally enters .../California instead of ../california, 301 them 
        if (!request.getRequestURI().equals(request.getRequestURI().toLowerCase()))
            return new ModelAndView(new RedirectView301(request.getRequestURI().toLowerCase()));

        boolean national = false;
        if (request.getRequestURI().endsWith("schools/")) {
            national = true;
            state = State.CA;
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(MODEL_STATE_NAME, state.getLongName());
        model.put(MODEL_STATE_ABBREVIATION, state.getAbbreviation());
        model.put(MODEL_NATIONAL, national);
        model.put(MODEL_TOP_SCHOOLS, getTopSchools(state));
        model.put(MODEL_ALL_STATES, StateManager.getList());
        return new ModelAndView(_viewName, model);
    }

    protected List<TopSchool> getTopSchools(State state) {
        String key = state.getAbbreviation().toLowerCase() + "_top_schools";
        Element cacheElement = _cache.get(key);
        List<TopSchool> topSchools;
        if (cacheElement == null) {
            topSchools = new ArrayList<TopSchool>();
            List<School> schools = _schoolDao.getTopSchools(state);
            for (School school : schools) {
                TopSchoolCategory category = (TopSchoolCategory) _schoolDao.getTopSchoolCategories(school).toArray()[0];
                topSchools.add(new TopSchool(school, category, getReviewText(school)));
            }
            _cache.put(new Element(key, topSchools));
        } else {
            topSchools = (List<TopSchool>) cacheElement.getObjectValue();
        }
        return topSchools;
    }

    protected String getReviewText(School school) {
        String reviewText = "";
        List<Review> reviews = _reviewDao.getPublishedReviewsBySchool(school);
        for (Review review : reviews) {
            CategoryRating rating = review.getQuality();
            if (rating.equals(CategoryRating.RATING_4) || rating.equals(CategoryRating.RATING_5)) {
                reviewText = review.getComments();
            }
        }
        return reviewText;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

    /**
     * We create a school value object for 2 reasons
     * 1. So we can ehCache it efficiently. Also, Hibernate objects get cglib'd so they are troublesome to cache.
     * 2. By subclassing we can also pass along additional data to the view without rewriting a bunch of getters
     */
    public class TopSchool extends School {
        TopSchoolCategory _category;
        String _reviewText;

        public TopSchool(School school, TopSchoolCategory category, String reviewText) {
            _category = category;
            setId(school.getId());
            setName(school.getName());
            setCity(school.getCity());
            setDatabaseState(school.getDatabaseState());
            setLevelCode(school.getLevelCode());
            setType(school.getType());
            _reviewText = StringUtils.abbreviate(reviewText, 40);
        }

        public String getReviewText() {
            return _reviewText;
        }

        public TopSchoolCategory getTopSchoolCategory() {
            return _category;
        }
    }
}
