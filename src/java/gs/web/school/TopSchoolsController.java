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
import gs.data.school.review.Poster;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.data.geo.City;
import gs.data.geo.IGeoDao;
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
    public static final String MODEL_COMPARE_CITIES = "compareCities";
    public static final int HOURS = 3600;

    protected String _viewName;
    protected ISchoolDao _schoolDao;
    protected static Cache _schoolCache;
    protected IReviewDao _reviewDao;
    protected ITableDao _tableDao;
    protected IGeoDao _geoDao;

    static {
        // Cache active top schools for 3 hrs for performance
        _schoolCache = new Cache(TopSchoolsController.class.getName() + ".schools", 51, false, false, 3 * HOURS, 1 * HOURS);
        CacheManager.create().addCache(_schoolCache);
    }

    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        State state = context.getState();

        if (!isUrlCanonical(request))
            return getCanonicalUrlRedirect(state);

        boolean national = isNational(request);
        if (national)
            if (state == null) state = State.CA;
            else return getCanonicalUrlRedirect(state);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(MODEL_STATE_NAME, state.getLongName());
        model.put(MODEL_STATE_ABBREVIATION, state.getAbbreviation());
        model.put(MODEL_NATIONAL, national);
        if (!national) model.put(MODEL_TOP_SCHOOLS, getTopSchools(state));
        model.put(MODEL_WHAT_MAKES_A_SCHOOL_GREAT, getWhatMakesASchoolGreatContent());
        model.put(MODEL_ALL_STATES, StateManager.getList());
        model.put(MODEL_COMPARE_CITIES, getCityList(state));
        return new ModelAndView(_viewName, model);
    }

    private boolean isNational(HttpServletRequest request) {
        return request.getRequestURI().endsWith("schools/");
    }

    private ModelAndView getCanonicalUrlRedirect(State state) {
        TopSchoolsUrl url = new TopSchoolsUrl(state);
        return new ModelAndView(new RedirectView301(url.getRelativePath()));
    }

    private boolean isUrlCanonical(HttpServletRequest request) {
        return request.getRequestURI().equals(request.getRequestURI().toLowerCase()) && request.getRequestURI().endsWith("/");
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
                Object[] review = getReviewText(school);
                topSchools.add(new TopSchool(school, category, (String) review[0], (Boolean) review[1]));
            }
            Collections.sort(topSchools);
            topSchools.get(topSchools.size() - 1).setLast(true);
            _schoolCache.put(new Element(key, topSchools));
        } else {
            topSchools = (List<TopSchool>) cacheElement.getObjectValue();
        }
        return topSchools;
    }

    /**
     * Return ESP school vision if it exists, otherwise return a parent review where the review is
     * from a parent and 4 or 5 stars (preferring 5 stars).
     */
    protected Object[] getReviewText(School school) {
        String reviewText = null;
        boolean reviewSubmitterParent = true;
        List<Review> reviews = _reviewDao.getPublishedReviewsBySchool(school);
        Collections.sort(reviews, Review.DATE_POSTED_COMPARATOR);
        for (CategoryRating rating : Arrays.asList(CategoryRating.RATING_4, CategoryRating.RATING_5))
            for (Review review : reviews)
                if (rating.equals(review.getQuality()) && Poster.PARENT.equals(review.getPoster()))
                    reviewText = review.getComments();
        return new Object[]{reviewText, reviewSubmitterParent};
    }

    protected List<ContentLink> getWhatMakesASchoolGreatContent() {
        List<ContentLink> contents;
        contents = new ArrayList<ContentLink>();
        for (ITableRow row : _tableDao.getAllRows()) {
            String link = row.getString("link");
            if (link != null && link.trim().length() > 0)
                contents.add(new ContentLink(row.getString("title"), link,
                        row.getString("target"), row.getString("class"), row.getString("text")));
        }
        return contents;
    }

    protected List<City> getCityList(State state) {
        List<City> cities = _geoDao.findCitiesByState(state);
        City city = new City();
        city.setName("Choose city");
        cities.add(0, city);
        return cities;
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

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    /**
     * We create a school value object for 2 reasons
     * 1. So we can ehCache it efficiently. Also, Hibernate objects get cglib'd so they are troublesome to cache.
     * 2. By subclassing we can also pass along additional data to the view without rewriting a bunch of getters
     */
    public class TopSchool extends School implements Comparable {
        TopSchoolCategory _category;
        String _reviewText;
        boolean _reviewSubmitterParent;
        boolean _last = false;

        public TopSchool(School school, TopSchoolCategory category, String reviewText, boolean reviewSubmitterParent) {
            _category = category;
            setId(school.getId());
            setName(school.getName());
            setCity(school.getCity());
            setDatabaseState(school.getDatabaseState());
            setLevelCode(school.getLevelCode());
            setType(school.getType());
            if (reviewText != null && reviewText.length() > 0) {
                _reviewText = "\"" + StringUtils.abbreviate(reviewText, 140) + "\"";
                _reviewSubmitterParent = reviewSubmitterParent;
            }
        }

        public String getReviewText() {
            return _reviewText;
        }

        public TopSchoolCategory getTopSchoolCategory() {
            return _category;
        }

        public boolean isReviewSubmitterParent() {
            return _reviewSubmitterParent;
        }

        /**
         * Sort top schools by their top school category type so they display in the correct order
         */
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
