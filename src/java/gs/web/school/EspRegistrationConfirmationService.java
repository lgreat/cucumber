package gs.web.school;

import gs.data.community.User;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.school.*;
import gs.data.security.Role;
import gs.data.state.State;
import gs.web.school.usp.EspStatus;
import gs.web.school.usp.EspStatusManager;
import gs.web.school.usp.UspFormController;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EspRegistrationConfirmationService implements BeanFactoryAware {

    @Autowired
    private ISchoolDao _schoolDao;
    @Autowired
    private IEspMembershipDao _espMembershipDao;
    @Autowired
    private IEspResponseDao _espResponseDao;
    @Autowired
    private EspRegistrationHelper _espRegistrationHelper;
    @Autowired
    @Qualifier("exactTargetAPI")
    private ExactTargetAPI _exactTargetAPI;

    private BeanFactory _beanFactory;

    public static enum EspSubmissionStatus {
        USP_SUBMITTED,
        OSP_VERIFIED,
        OSP_PROVISIONAL_UPGRADED,
        OSP_PROVISIONAL_NOT_UPGRADED,
        NO_ESP_SUBMISSION
    }

    public EspSubmissionStatus handleEspSubmissions(HttpServletRequest request, User user) {

        if (StringUtils.isNotBlank(request.getParameter("schoolId")) && StringUtils.isNotBlank(request.getParameter("state"))
                && StringUtils.isNotBlank(UspFormController.PARAM_USP_SUBMISSION)) {
            //If its a USP submission then check if the school is in the right status and activate the usp data.
            School school = null;
            try {
                Integer schoolId = Integer.parseInt(request.getParameter("schoolId"));
                State state = State.fromString(request.getParameter("state"));
                school = _schoolDao.getSchoolById(state, schoolId);
            } catch (Exception ex) {
                school = null;
            }

            if (school != null && school.isActive()) {
                //Use the BeanFactoryAware so that we get the espStatusManager component with auto injections.Otherwise we have to
                //manually set the espResponseDao on the espStatusManager.
                EspStatusManager statusManager = (EspStatusManager) _beanFactory.getBean("espStatusManager", new Object[]{school});
                EspStatus espStatus = statusManager.getEspStatus();

                //If the school is in osp preferred status then do not activate the data.
                if (!espStatus.equals(EspStatus.OSP_PREFERRED)) {
                    List<EspResponse> responses = _espResponseDao.getResponses(school, user.getId(), true);
                    if (!responses.isEmpty()) {
                        //TODO Do we need to keep the inactive responses?
                        _espResponseDao.activateResponses(school, user.getId(), EspResponseSource.usp);

                        // send thank you email after responses are activated on email verification
                        Map<String, String> emailAttributes = new HashMap<String, String>();
                        emailAttributes.put("school_name", school.getName());
                        UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
                        emailAttributes.put("school_URL", urlBuilder.asFullUrl(request));
                        getExactTargetAPI().sendTriggeredEmail("USP-thank-you", user, emailAttributes);
                    }
                }
            }

            return EspSubmissionStatus.USP_SUBMITTED;
        } else if (user.hasRole(Role.ESP_MEMBER)) {
            return EspSubmissionStatus.OSP_VERIFIED;
        } else {
            // check if user has an esp membership row in processing state
            EspMembership membership = getProcessingMembershipForUser(user);
            if (membership != null && membership.getSchoolId() != null && membership.getState() != null) {
                boolean isUserEligibleForProvisional = _espRegistrationHelper.isMembershipEligibleForProvisionalStatus(membership.getSchoolId(), membership.getState());
                if (isUserEligibleForProvisional) {
                    // bump this user to provisional
                    membership.setStatus(EspMembershipStatus.PROVISIONAL);
                    membership.setActive(false);
                    _espMembershipDao.updateEspMembership(membership);
                    return EspSubmissionStatus.OSP_PROVISIONAL_UPGRADED;
                } else {
                    return EspSubmissionStatus.OSP_PROVISIONAL_NOT_UPGRADED;
                }
            }
        }
        return EspSubmissionStatus.NO_ESP_SUBMISSION;
    }

    protected EspMembership getProcessingMembershipForUser(User user) {
        List<EspMembership> memberships = _espMembershipDao.findEspMembershipsByUserId(user.getId(), false);
        if (memberships != null && memberships.size() > 0) {
            for (EspMembership membership : memberships) {
                if (membership.getStatus() == EspMembershipStatus.PROCESSING) {
                    return membership;
                }
            }
        }
        return null;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        _beanFactory = beanFactory;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public void setEspMembershipDao(IEspMembershipDao espMembershipDao) {
        _espMembershipDao = espMembershipDao;
    }

    public void setEspResponseDao(IEspResponseDao espResponseDao) {
        _espResponseDao = espResponseDao;
    }

    public void setEspRegistrationHelper(EspRegistrationHelper espRegistrationHelper) {
        _espRegistrationHelper = espRegistrationHelper;
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI _exactTargetAPI) {
        this._exactTargetAPI = _exactTargetAPI;
    }
}