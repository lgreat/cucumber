package gs.web.school;

import gs.data.community.User;
import gs.data.school.*;
import gs.web.util.UrlBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Helper for signin and registration controllers
 * User: rraker
 * Date: 3/27/13
 * Time: 10:31 AM
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

    /**
     * Determine the next view based on the user's status  In reality this is mostly driven off of the users EspMembership's
     * @param request
     * @param user
     * @return
     */
    public String determineNextView(HttpServletRequest request, User user) {
        // Verify we have a user
        if( user==null || user.getId()==0 ) {
            return null;
        }

        List<EspMembership> memberships = _espMembershipDao.findEspMembershipsByUserId(user.getId(), true );

        // If no memberships - send the user to the "short" registration page with the existing data filled in
        if( memberships == null || memberships.size() == 0 ) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_REGISTRATION);
            return "redirect:" + urlBuilder.asFullUrl(request);
        }

        EspMembership approved = checkMembershipStatus( memberships, EspMembershipStatus.APPROVED );
        if( approved != null ) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
            return "redirect:" + urlBuilder.asFullUrl(request);
        }

        EspMembership processing = checkMembershipStatus( memberships, EspMembershipStatus.PROCESSING );
        if( processing != null ) {
            // When in Processing status, see isMembership is eligible for promotion to provisional the user can be upgraded to PROVISIONAL
            boolean eligible = isMembershipEligibleForPromotionToProvisional(processing);
            if( eligible ) {
                processing.setStatus( EspMembershipStatus.PROVISIONAL);
                _espMembershipDao.updateEspMembership(processing);
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
                urlBuilder.addParameter("message", "provisional");
                return "redirect:" + urlBuilder.asFullUrl(request);
            }
            else {
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_REGISTRATION_ERROR);
                urlBuilder.addParameter("message", "page1");
                return "redirect:" + urlBuilder.asFullUrl(request);
            }
        }

        EspMembership provisional = checkMembershipStatus( memberships, EspMembershipStatus.PROVISIONAL );
        if( provisional != null ) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
            return "redirect:" + urlBuilder.asFullUrl(request);
    }

        EspMembership deactivated = checkMembershipStatus( memberships, EspMembershipStatus.DISABLED );
        if( deactivated == null ) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_REGISTRATION_ERROR);
            urlBuilder.addParameter("message", "page6");
            return "redirect:" + urlBuilder.asFullUrl(request);
        }

        EspMembership rejected = checkMembershipStatus( memberships, EspMembershipStatus.REJECTED );
        if( rejected == null ) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_REGISTRATION_ERROR);
            urlBuilder.addParameter("message", "page5");
            return "redirect:" + urlBuilder.asFullUrl(request);
        }

        //  If we happen to get here just return null and let the caller decide what to do
        return null;
    }

    /**
     * Check all memberships for a matching status
     * @param memberships
     * @param processing
     * @return
     */
    private EspMembership checkMembershipStatus(List<EspMembership> memberships, EspMembershipStatus processing) {
        for( EspMembership espMembership : memberships ) {
            if( processing.equals(espMembership.getStatus())) {
                return espMembership;
            }
        }
        return null;
    }

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
