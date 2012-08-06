package gs.web.school;


import gs.data.school.School;
import gs.data.school.census.CensusCacheDaoHibernate;
import gs.data.state.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/admin/school/census/cachemanager")
@Component("censusCacheManager")
public class CensusCacheManager {

    @Autowired
    CensusCacheDaoHibernate _censusCacheDaoHibernate;

    /**
     *
     * @param state
     * @param schoolId
     */
    @RequestMapping(method= RequestMethod.DELETE)
    public void delete(@RequestParam("state") State state, @RequestParam("schoolId") Integer schoolId) {
        if (state != null && schoolId != null) {
            _censusCacheDaoHibernate.deleteBySchoolId(state, schoolId);
        }
    }

    public void deleteBySchool(School school) {
        delete(school.getDatabaseState(), school.getId());
    }

}
