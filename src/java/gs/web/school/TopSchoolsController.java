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
    public static final String MODEL_WHAT_MAKES_A_SCHOOL_GREAT = "whatMakesASchoolGreat";
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
        if (national) {
        } else {
            model.put(MODEL_TOP_SCHOOLS, getTopSchools(state));
        }
        model.put(MODEL_WHAT_MAKES_A_SCHOOL_GREAT, getWhatMakesASchoolGreatContent());
        model.put(MODEL_ALL_STATES, StateManager.getList());
        return new ModelAndView(_viewName, model);
    }

    protected List<ContentLink> getWhatMakesASchoolGreatContent() {
        List<ContentLink> contents = new ArrayList<ContentLink>();
        contents.add(new ContentLink("View from the top", "/", "_blank", "Bill Jackson, Greatschools founder and President, reveals the secrets behind a great school"));
        contents.add(new ContentLink("Talk it out", "http://community.greatschools.net", "", "Share with other parents what makes a school great, and whether your school makes the grade"));
        contents.add(new ContentLink("Field trip", "/", "_blank", "Visit BusinessWeek to find out why these schools made the list"));
        return contents;
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
            Collections.sort(topSchools);
            topSchools.get(topSchools.size() - 1).setLast(true);
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
    public class TopSchool extends School implements Comparable {
        TopSchoolCategory _category;
        String _reviewText;
        boolean _last = false;

        public TopSchool(School school, TopSchoolCategory category, String reviewText) {
            _category = category;
            setId(school.getId());
            setName(school.getName());
            setCity(school.getCity());
            setDatabaseState(school.getDatabaseState());
            setLevelCode(school.getLevelCode());
            setType(school.getType());
            _reviewText = StringUtils.abbreviate(reviewText, 95);
            _reviewText = StringUtils.abbreviate("this is a test this is a test this is a test this is a test this is a test this is a test this is a test this is a test this is a test this is a test this is a test this is a test this is a test this is a test ", 95);
        }

        public String getReviewText() {
            return _reviewText;
        }

        public TopSchoolCategory getTopSchoolCategory() {
            return _category;
        }

        public int compareTo(Object school) {
            return _category.getCode() - ((TopSchool) school).getTopSchoolCategory().getCode();
        }

        public boolean isLast() {
            return _last;
        }

        public void setLast(boolean last) {
            _last = last;
        }
    }

    public class ContentLink {
        String _title;
        String _link;
        String _target;
        String _text;

        public ContentLink(String title, String link, String target, String text) {
            _title = title;
            _link = link;
            _target = target;
            _text = text;
        }

        public String getTitle() {
            return _title;
        }

        public String getLink() {
            return _link;
        }

        public String getTarget() {
            return _target;
        }

        public String getText() {
            return _text;
        }
    }
}
