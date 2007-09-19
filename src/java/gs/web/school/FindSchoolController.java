package gs.web.school;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.*;
import gs.data.state.State;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class FindSchoolController extends AbstractCommandController {

    private ISchoolDao _schoolDao;
    private IGeoDao _geoDao;

    private static enum Filter {
        school, city
    }

    FindSchoolController() {
        super();
        setCommandClass(FindSchoolCommand.class);
    }

    protected void initBinder(HttpServletRequest request,
                              ServletRequestDataBinder binder) {
        binder.registerCustomEditor(Filter.class, new FilterCustomProperyEditor());
        binder.registerCustomEditor(LevelCode.Level.class, new LevelEditor());
    }

    static class FilterCustomProperyEditor extends PropertyEditorSupport {
        public String getAsText() {
            Filter filter = (Filter) getValue();
            return filter.name();
        }

        public void setAsText(final String text) {
            setValue(Filter.valueOf(text));
        }
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        FindSchoolCommand fsc = (FindSchoolCommand) command;

        if (Filter.school.equals(fsc.getFilter())) {
            List<School> schools = getSchoolDao().findSchoolsInCity(fsc.getState(), fsc.getCity(), false);
            Collection<School> filteredSchools = CollectionUtils.select(schools, LevelPredicateFactory.createLevelPredicate(fsc.getLevel()));
            getSchoolJson(response, filteredSchools);
        } else {
            List<City> cities = getGeoDao().findCitiesByState(fsc.getState());
            getCityJson(response, cities);
        }

        return null;
    }

    protected void getSchoolJson(HttpServletResponse response, Collection<School> schools) throws Exception {
        StringBuffer buff = new StringBuffer(4000);
        buff.append("{\"schools\":[");

        for (Iterator<School> iter = schools.iterator(); iter.hasNext();) {
            School school = iter.next();
            buff.append(generateSchoolJsonObject(school));
            if (iter.hasNext()) {
                buff.append(",");
            }
        }
        buff.append("]}");
        response.setContentType("text/x-json");
        response.getWriter().print(buff.toString());
        response.getWriter().flush();
    }

    protected String generateSchoolJsonObject(School school) {
        return "{\"id\":" + school.getId() + ",\"name\":\"" + StringEscapeUtils.escapeJavaScript(school.getName()) + "\"}";
    }

    protected void getCityJson(HttpServletResponse response, List<City> cities) throws Exception {
        StringBuffer buff = new StringBuffer(4000);
        buff.append("{\"cities\":[");

        for (Iterator<City> iter = cities.iterator(); iter.hasNext();) {
            City city = iter.next();
            buff.append("{\"name\":\"").append(StringEscapeUtils.escapeJavaScript(city.getName())).append("\"}");
            if (iter.hasNext()) {
                buff.append(",");
            }
        }
        buff.append("]}");
        response.setContentType("text/x-json");
        response.getWriter().print(buff.toString());
        response.getWriter().flush();
    }

    public static class FindSchoolCommand {
        private LevelCode.Level _level;
        private State _state;
        private String _city;
        private Filter _filter;

        public LevelCode.Level getLevel() {
            return _level;
        }

        public void setLevel(LevelCode.Level level) {
            _level = level;
        }

        public State getState() {
            return _state;
        }

        public void setState(State state) {
            _state = state;
        }

        public String getCity() {
            return _city;
        }

        public void setCity(String city) {
            _city = city;
        }

        public Filter getFilter() {
            return _filter;
        }

        public void setFilter(Filter filter) {
            _filter = filter;
        }
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }
}
