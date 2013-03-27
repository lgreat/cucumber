package gs.web.school;

import gs.data.school.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rraker
 * Date: 3/27/13
 * Time: 10:31 AM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class EspRegistrationHelper {

    protected final Log _log = LogFactory.getLog(getClass());

    @Autowired
    private IEspMembershipDao _espMembershipDao;
    @Autowired
    private IEspResponseDao _espResponseDao;
    @Autowired
    private ISchoolDao _schoolDao;

    public boolean isMembershipEligibleForPromotionToProvisional(EspMembership membership) {
        List<EspMembership> schoolMemberships;
        School school;
        try {
            school = getSchoolDao().getSchoolById
                    (membership.getState(), membership.getSchoolId());
            if (school == null) {
                return false;
            }
            schoolMemberships = _espMembershipDao.findEspMembershipsBySchool(school, false);
        } catch (Exception e) {
            _log.error("Can't find school for membership: " + membership, e);
            return false;
        }
        // if there is no existing provisional membership
        if (schoolMemberships != null && schoolMemberships.size() > 1) {
            for (EspMembership schoolMembership: schoolMemberships) {
                if (schoolMembership.getStatus() == EspMembershipStatus.PROVISIONAL) {
                    return false;
                }
            }
        }
        // check most recent timestamp on active rows in esp_response for the school
        Date maxCreated = getEspResponseDao().getMaxCreatedForSchool(school, false);
        if (maxCreated == null) {
            // no esp responses, so we're golden
            return true;
        }
        Calendar aWeekAgo = Calendar.getInstance(); aWeekAgo.add(Calendar.DAY_OF_YEAR, -7);
        Calendar lastModified = Calendar.getInstance(); lastModified.setTime(maxCreated);

        // if that date is older than 7 days
        return lastModified.before(aWeekAgo);
    }

    public IEspMembershipDao getEspMembershipDao() {
        return _espMembershipDao;
    }

    public void setEspMembershipDao(IEspMembershipDao espMembershipDao) {
        _espMembershipDao = espMembershipDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IEspResponseDao getEspResponseDao() {
        return _espResponseDao;
    }

    public void setEspResponseDao(IEspResponseDao espResponseDao) {
        _espResponseDao = espResponseDao;
    }

}
