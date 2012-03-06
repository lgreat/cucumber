package gs.web.school;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.EspMembership;
import gs.data.school.EspMembershipStatus;
import gs.data.school.IEspMembershipDao;
import gs.data.school.ISchoolDao;
import gs.data.util.DigestUtil;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author aroy@greatschools.org
 */
@Controller
@RequestMapping("/official-school-profile/")
public class EspPreRegistrationController implements ReadWriteAnnotationController {
    public static final String PATH_TO_FORM = "/official-school-profile/preRegister.page"; // used by UrlBuilder
    private static final Log _log = LogFactory.getLog(EspPreRegistrationController.class);

    public static final String VIEW = "school/espPreRegistration";
    
    public static final String PARAM_ID="id";
    public static final String MODEL_MEMBERSHIP = "membership";
    public static final String MODEL_USER = "user";

    @Autowired
    private IEspMembershipDao _espMembershipDao;
    @Autowired
    private IUserDao _userDao;
    @Autowired
    private ISchoolDao _schoolDao; // TODO: maybe unnecessary?

    @RequestMapping(value = "preRegister.page", method = RequestMethod.GET)
    public String showForm(ModelMap modelMap, HttpServletRequest request) {
        String hashPlusId = request.getParameter(PARAM_ID);
        if (StringUtils.isBlank(hashPlusId)) {
            _log.error("No id parameter");
            return redirectToRegistration(request);
        }

        // validate hashPlusId (see EmailVerificationLinkValidator)
        // this should also give us a User object
        // Note we're ignoring cookies here, we only care about who the user encoded in the link is
        User user = getValidUserFromHash(hashPlusId);
        if (user == null) {
            // error logging handled by getValidUserFromHash method
            return redirectToRegistration(request);
        }
        // fetch memberships for user
        List<EspMembership> memberships = _espMembershipDao.findEspMembershipsByUserId(user.getId(), false);
        // error check
        if (memberships == null || memberships.size() == 0) {
            _log.error("No memberships found for user " + user);
            return redirectToRegistration(request);
        }
        // pull the membership in status PRE_APPROVED
        boolean hasActiveMembership = false;
        EspMembership membershipToProcess = null;
        for (EspMembership membership: memberships) {
            if (membership.getStatus() == EspMembershipStatus.PRE_APPROVED) {
                membershipToProcess = membership;
            } else if (membership.getActive()) {
                hasActiveMembership = true;
            }
        }
        if (membershipToProcess == null) {
            _log.error("No pre_approved memberships found for user " + user);
            return redirectToRegistration(request);
        } else if (hasActiveMembership) {
            // What to do here? For now we only allow one active membership per user, so error
            _log.error("Already found active membership for user " + user);
            return redirectToRegistration(request);
        }
        
        // ok member passed authentication and has a preapproved membership, let's show them the form!
        // TODO: Any other data necessary?
        modelMap.put(MODEL_MEMBERSHIP, membershipToProcess);
        modelMap.put(MODEL_USER, user);
        
        return VIEW;
    }

    @RequestMapping(value = "preRegister.page", method = RequestMethod.POST)
    public String processForm(ModelMap modelMap, HttpServletRequest request) {
        // TODO
        return null;
    }
    
    protected User getValidUserFromHash(String hashPlusUserId) {
        User user = null;
        Integer userId = getUserId(hashPlusUserId);
        if (hashPlusUserId == null) {
            _log.error("Cannot verify email with null hashPlusUserId");
        } else if (hashPlusUserId.length() <= DigestUtil.MD5_HASH_LENGTH || userId == null) {
            _log.warn("Email verification request with badly formed hashPlusUserId: " + hashPlusUserId +
                    "Expecting hash of length " + DigestUtil.MD5_HASH_LENGTH + " followed by userId.");
        } else {
            String hash = getHash(hashPlusUserId);
            try {
                user = _userDao.findUserFromId(userId);
                if (!verificationHashMatchesUser(user, hash)) {
                    _log.error("Verification link hash " + hashPlusUserId + " does not match user: " + user);
                }
            } catch (ObjectRetrievalFailureException orfe) {
                _log.warn("Community registration request for unknown user id: " + userId);
            }
        }

        return user;
    }

    public boolean verificationHashMatchesUser(User user, String hash) {
        boolean validHash = false;
        String actualHash = null;
        try {
            if (user.getId() != null && user.getEmail() != null) {
                actualHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            }
            validHash = (hash != null && actualHash != null && hash.equals(actualHash));
            if (!validHash) {
                _log.warn("OSP Pre-registration request has invalid hash: " + hash + " for user " + user.getEmail());
//                _log.error("TEMPORARILY RETURNING TRUE DURING DEVELOPMENT. DO NOT CHECK IN");
//                return true;
            }
        } catch (NoSuchAlgorithmException e) {
            _log.warn("Failed to hash string: " + e, e);
            //Nothing can be done
        }
        return validHash;
    }

    public String getHash(String hashPlusUserId) {
        if (hashPlusUserId == null || hashPlusUserId.length() <= DigestUtil.MD5_HASH_LENGTH) {
            return null;
        }

        return hashPlusUserId.substring(0, DigestUtil.MD5_HASH_LENGTH);
    }

    public Integer getUserId(String hashPlusUserId) {
        if (hashPlusUserId == null || hashPlusUserId.length() <= DigestUtil.MD5_HASH_LENGTH) {
            return null;
        }

        try {
            return Integer.parseInt(hashPlusUserId.substring(DigestUtil.MD5_HASH_LENGTH));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    
    protected String redirectToRegistration(HttpServletRequest request) {
        UrlBuilder ospReg = new UrlBuilder(UrlBuilder.ESP_REGISTRATION);
        return "redirect:" + ospReg.asSiteRelative(request);
    }
}
