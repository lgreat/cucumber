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
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
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
    protected static Cache _schoolCache;
    protected static Cache _articleCache;
    protected IReviewDao _reviewDao;
    protected ITableDao _tableDao;

    static {
        // Cache active top schools for 3 hrs for performance, articles for 10 minutes
        CacheManager manager = CacheManager.create();
        _schoolCache = new Cache(TopSchoolsController.class.getName() + ".schools", 51, false, false, 10800, 3600);
        _articleCache = new Cache(TopSchoolsController.class.getName() + ".articles", 5, false, false, 600, 600);
        manager.addCache(_schoolCache);
        manager.addCache(_articleCache);
    }

    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        State state = context.getState();

        // If someone accidentally enters .../California instead of ../california, 301 them 
        if (!request.getRequestURI().equals(request.getRequestURI().toLowerCase()))
            return new ModelAndView(new RedirectView301(request.getRequestURI().toLowerCase()));

        boolean national = false;
        if (request.getRequestURI().endsWith("schools/")) {
            if (state == null) {
                national = true;
                state = State.CA;
            } else {
                // If the user has a state cookie redirect them
                return new ModelAndView(new RedirectView301("/top-high-schools/" + state.getLongName().toLowerCase().replace(" ","-")));
            }
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(MODEL_STATE_NAME, state.getLongName());
        model.put(MODEL_STATE_ABBREVIATION, state.getAbbreviation());
        model.put(MODEL_NATIONAL, national);
        if (!national) model.put(MODEL_TOP_SCHOOLS, getTopSchools(state));
        model.put(MODEL_WHAT_MAKES_A_SCHOOL_GREAT, getWhatMakesASchoolGreatContent());
        model.put(MODEL_ALL_STATES, StateManager.getList());
        return new ModelAndView(_viewName, model);
    }

    protected List<ContentLink> getWhatMakesASchoolGreatContent() {
        String key = "what_makes_a_school_great";
        Element cacheElement = _articleCache.get(key);
        List<ContentLink> contents;
        if (cacheElement == null) {
            contents = new ArrayList<ContentLink>();
            for (ITableRow row : _tableDao.getAllRows()) {
                String link = row.getString("link");
                if (link != null && link.trim().length() > 0)
                    contents.add(new ContentLink(row.getString("title"), link,
                            row.getString("target"), row.getString("class"), row.getString("text")));
            }
            _articleCache.put(new Element(key, contents));
        } else {
            contents = (List<ContentLink>) cacheElement.getObjectValue();
        }
        return contents;
    }

    protected List<TopSchool> getTopSchools(State state) {
        String key = state.getAbbreviation().toLowerCase() + "_top_schools";
        Element cacheElement = _schoolCache.get(key);
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
            _schoolCache.put(new Element(key, topSchools));
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

    public void setTableDao(ITableDao tableDao) {
        _tableDao = tableDao;
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
        String _styleClass;
        String _text;

        public ContentLink(String title, String link, String target, String styleClass, String text) {
            _title = title;
            _link = link;
            _target = (target != null && target.length() > 3) ? target : "_self";
            _styleClass = styleClass;
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

        public String getStyleClass() {
            return _styleClass;
        }

        public String getText() {
            return _text;
        }
    }
}
