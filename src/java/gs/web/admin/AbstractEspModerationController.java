package gs.web.admin;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.school.EspMembership;
import gs.data.school.EspMembershipStatus;
import gs.data.school.EspResponse;
import gs.data.school.IEspMembershipDao;
import gs.data.school.IEspResponseDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.security.IRoleDao;
import gs.data.security.Role;
import gs.data.state.INoEditDao;
import gs.data.util.Address;
import gs.data.util.DigestUtil;
import gs.web.school.EspSaveBehaviour;
import gs.web.school.EspSaveHelper;
import gs.web.school.OspSaveBehaviour;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.tracking.OmnitureTracking;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlBuilder;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;

/**
 * @author jkirton
 */
public abstract class AbstractEspModerationController implements ReadWriteAnnotationController {

    /**
     * Dedicated data struct for each moderation row.
     */
    public static class ModerationRow {
        private EspMembership _membership;
        private String _contactName;
        private String _contactEmail;
        private boolean _isDisabledUserReRequestingAccess;
        private boolean _hasOtherActiveEspMemberships;
        private boolean _schoolHasActiveMemberships;
        private boolean _userHasProvisionalAccess;

        public ModerationRow(EspMembership _membership) {
            super();
            this._membership = _membership;
        }

        public EspMembership getMembership() {
            return _membership;
        }

        public String getContactName() {
            return _contactName;
        }

        public void setContactName(String contactName) {
            this._contactName = contactName;
        }

        public String getContactEmail() {
            return _contactEmail;
        }

        public void setContactEmail(String contactEmail) {
            this._contactEmail = contactEmail;
        }

        public boolean isDisabledUserReRequestingAccess() {
            return _isDisabledUserReRequestingAccess;
        }

        public void setDisabledUserReRequestingAccess(boolean isDisabledUserReRequestingAccess) {
            this._isDisabledUserReRequestingAccess = isDisabledUserReRequestingAccess;
        }

        public boolean isHasOtherActiveEspMemberships() {
            return _hasOtherActiveEspMemberships;
        }

        public void setHasOtherActiveEspMemberships(boolean hasOtherActiveEspMemberships) {
            _hasOtherActiveEspMemberships = hasOtherActiveEspMemberships;
        }

        public boolean isEmailMatch() {
            if(_contactEmail == null || _membership.getUser() == null) return false;
            return _contactEmail.equals(_membership.getUser().getEmail());
        }

        public String getAbsoluteWebUrl() {
            String webUrl = _membership.getWebUrl();
            if (StringUtils.isNotEmpty(webUrl) && !StringUtils.startsWithIgnoreCase(webUrl, "http://")) {
                return "http://" + webUrl;
            }
            return webUrl;
        }

        public boolean isSchoolHasActiveMemberships() {
            return _schoolHasActiveMemberships;
        }

        public void setSchoolHasActiveMemberships(boolean schoolHasActiveMemberships) {
            _schoolHasActiveMemberships = schoolHasActiveMemberships;
        }

        public boolean isUserHasProvisionalAccess() {
            return _userHasProvisionalAccess;
        }

        public void setUserHasProvisionalAccess(boolean userHasProvisionalAccess) {
            _userHasProvisionalAccess = userHasProvisionalAccess;
        }
    }

    protected final Log _log = LogFactory.getLog(getClass());

    @Autowired
    protected IEspMembershipDao _espMembershipDao;

    @Autowired
    protected IUserDao _userDao;

    @Autowired
    protected ISchoolDao _schoolDao;

    @Autowired
    protected IRoleDao _roleDao;

    @Autowired
    protected IEspResponseDao _espResponseDao;

    protected ExactTargetAPI _exactTargetAPI;

    @Autowired
    private EspSaveHelper _espSaveHelper;

    @Autowired
    private INoEditDao _noEditDao;

    protected abstract String getViewName();

    /**
     * Updates esp membership rows contained in the given commmand/request.
     * @param command
     * @param request
     * @param response
     */
    protected void updateEspMembership(EspModerationCommand command, HttpServletRequest request, HttpServletResponse response) {
        //The user has to check the check boxes in order to approve or reject.Hence we iterate over the checked check boxes for those "approve" or "reject" actions.
        //The user does not have to check the check boxes for the update action.Hence we loop over all the notes for the "update" action.

        String moderatorAction = command.getModeratorAction();
        if(moderatorAction == null) return;

        if (("approve".equals(moderatorAction) || "reject".equals(moderatorAction) || moderatorAction.contains("deactivate"))
                && command.getEspMembershipIds() != null && !command.getEspMembershipIds().isEmpty()) {

            //The checkbox has a key of membership id.
            for (Integer membershipId : command.getEspMembershipIds()) {

                //get the membership row.
                EspMembership membership = getEspMembershipDao().findEspMembershipById(membershipId, false);
                if (membership != null) {

                    User user = membership.getUser();
                    if (user != null) {
                        boolean updateMembership = false;
                        try {
                            School school = getSchoolDao().getSchoolById(membership.getState(), membership.getSchoolId());
                            membership.setSchool(school);
                        } catch (Exception e) {
                            _log.error("Error fetching school for membership: " + membership, e);
                        }
                        if ("approve".equals(moderatorAction)) {
                            if (membership.getStatus() == EspMembershipStatus.PROVISIONAL
                                    && !membership.getActive()) {
                                if (_noEditDao.isStateLocked(membership.getSchool().getDatabaseState())) {
                                    _log.warn("State locked while promoting provisional user.State:" + membership.getSchool().getDatabaseState()
                                            + "User Id:" + membership.getUser().getId());
                                } else {
                                    promoteProvisionalDataToActiveData(user, membership.getSchool(), request, response);
                                    approveMembership(membership, EspMembershipStatus.APPROVED, true, user);
                                    sendESPApprovalEmail(user, membership.getSchool(),request);
                                    updateMembership = true;
                                }
                            } else if (membership.getStatus() == EspMembershipStatus.PROCESSING
                                    || membership.getStatus() == EspMembershipStatus.REJECTED || !membership.getActive()) {
                                approveMembership(membership, EspMembershipStatus.APPROVED, true, user);
                                sendESPVerificationEmail(user, membership.getSchool(),request);
                                updateMembership = true;
                            }
                        } else if ("reject".equals(moderatorAction)) {
                            if (membership.getStatus() == EspMembershipStatus.PROCESSING ||
                                    membership.getStatus() == EspMembershipStatus.PROVISIONAL) {
                                membership.setStatus(EspMembershipStatus.REJECTED);
                                membership.setActive(false);
                                sendRejectionEmail(user,membership.getSchool());
                                updateMembership = true;
                            }
                        } else if ("deactivate".equals(moderatorAction)) {
                            if (membership.getStatus() == EspMembershipStatus.APPROVED ||
                                    membership.getStatus() == EspMembershipStatus.PRE_APPROVED) {
                                membership.setActive(false);
                                membership.setStatus(EspMembershipStatus.DISABLED);
                                updateMembership = true;
                            }
                        }

                        //In case a note was added while approving or rejecting.
                        //Notes is the Map of membership id to string.
                        if (command.getNotes() != null && !command.getNotes().isEmpty()
                                && StringUtils.isNotBlank(command.getNotes().get(membership.getId()))) {
                            membership.setNote(command.getNotes().get(membership.getId()));
                            updateMembership = true;
                        }

                        if(updateMembership) {
                            membership.setUpdated(new Date());
                            getEspMembershipDao().updateEspMembership(membership);
//                            ThreadLocalTransactionManager.commitOrRollback();
                            //The ESP_MEMBER role needs to be removed if there are no more active memberships for the user.
                            removeEspRole(user);
                        }
                    }
                }
            }
        } else if ("update".equals(moderatorAction) && command.getNotes() != null && !command.getNotes().isEmpty()) {
            //Notes is the Map of membership id to string.
            for (int membershipId : command.getNotes().keySet()) {
                String noteVal = command.getNotes().get(membershipId);
                if (noteVal != null) {
                    //get the membership row.
                    EspMembership membership = getEspMembershipDao().findEspMembershipById(membershipId, false);
                    if (membership != null && !ObjectUtils.equals(noteVal, membership.getNote())) {
                        membership.setNote(noteVal);
                        membership.setUpdated(new Date());
                        getEspMembershipDao().updateEspMembership(membership);
                    }
                }
            }
        }
    }

    protected void approveMembership(EspMembership membership, EspMembershipStatus status, boolean active, User user) {
        membership.setStatus(status);
        membership.setActive(active);
        addEspRole(user);
    }

    /**
     * The provisional data provided by the provisional user needs to be promoted to active data.This method handles it.
     *
     * @param user
     * @param school
     * @return
     */
    protected void promoteProvisionalDataToActiveData(User user, School school, HttpServletRequest request, HttpServletResponse response) {

        Set<String> keysForPage = new HashSet<String>();
        Map<String, Object[]> keyToResponseMap = new HashMap<String, Object[]>();
        Map<String, List<Object>> requestParameterMap = new HashMap<String, List<Object>>();
        Map<String, String> errorFieldToMsgMap = new HashMap<String, String>();
        List<EspResponse> responseList = new ArrayList<EspResponse>();

        //Get all the provisional responses.
        List<EspResponse> espResponses = _espResponseDao.getResponses(school, user.getId(), true);

        if (espResponses != null && !espResponses.isEmpty()) {
            //Construct the list of key to responses Map.
            for (EspResponse espResponse : espResponses) {
                String key = espResponse.getKey();
                if (!key.startsWith("_page_")&& !espResponse.isActive()) {
                    List<Object> ojbs = new ArrayList<Object>();
                    if (requestParameterMap.get(key) != null) {
                        ojbs = requestParameterMap.get(key);
                    }
                    ojbs.add(espResponse.getValue());
                    requestParameterMap.put(key, ojbs);
                } else {
                    String[] keys = espResponse.getValue().split(",");
                    keysForPage.addAll(Arrays.asList(keys));
                }
            }

            //Perform conversions as required, since the handler methods perform type casting.
            //grade_levels:- convert the type(list of Object) to an array of Strings.
            //address :- convert the type Object into Address.
            //All other keys :- convert the type (list of Object) to an array of Objects.
            for (String key : requestParameterMap.keySet()) {
                if (key.equals("grade_levels")) {
                    String[] grades = requestParameterMap.get(key).toArray(new String[requestParameterMap.get(key).size()]);
                    keyToResponseMap.put(key, grades);
                } else if (key.equals("address")) {
                    String addressStr = requestParameterMap.get(key).get(0).toString();
                    Address address = Address.parseAddress(addressStr);
                    if (address != null) {
                        Object[] objects = new Object[1];
                        objects[0] = address;
                        keyToResponseMap.put(key, objects);
                    }
                } else {
                    keyToResponseMap.put(key, requestParameterMap.get(key).toArray());
                }
            }

            Set<Integer> provisionalMemberIds = new HashSet<Integer>();
            provisionalMemberIds.add(user.getId());

            // Check if this is the first time this school has gotten any data(exclude data by the user being approved).
            boolean schoolHasNoUserCreatedRows = _espResponseDao.schoolHasNoUserCreatedRows(school, true , provisionalMemberIds);

            OspSaveBehaviour saveBehaviour = new OspSaveBehaviour(false, true, false);
            _espSaveHelper.saveOspFormData(user, school, school.getDatabaseState(), -1, keysForPage, keyToResponseMap,
                    responseList, errorFieldToMsgMap, saveBehaviour);

            if (schoolHasNoUserCreatedRows) {
                OmnitureTracking omnitureTracking = new CookieBasedOmnitureTracking(request, response);
                omnitureTracking.addSuccessEvent(OmnitureTracking.SuccessEvent.NewEspStarted);
            }

        }
    }

    /**
     * Hook to remove elements before they are displayed. 
     * @param memberships list to display
     * @param modelMap map of model data
     */
    protected void filterMembershipRows(List<EspMembership> memberships, ModelMap modelMap) {
        // no-op
    }

    protected void populateModelWithMemberships(List<EspMembership> memberships, ModelMap modelMap) {
        // filter rows
        filterMembershipRows(memberships, modelMap);

        List<ModerationRow> mrows = new ArrayList<ModerationRow>();
        //List<EspMembership> approvedMemberships = new ArrayList<EspMembership>();
        List<EspMembership> rejectedMemberships = new ArrayList<EspMembership>();

        // first create rejected sublist
        for (EspMembership membership : memberships) {
            if(membership.getStatus() == EspMembershipStatus.REJECTED) {
                rejectedMemberships.add(membership);
            }
        }

        for (EspMembership membership : memberships) {
            long schoolId = membership.getSchoolId();
            School school;
            try {
                school = getSchoolDao().getSchoolById(membership.getState(), (int) schoolId);
            } catch (Exception e) {
                _log.error("Error fetching school for membership: " + membership, e);
                continue;
            }
            membership.setSchool(school);

            ModerationRow mrow = new ModerationRow(membership);
            mrows.add(mrow);

            // contact name and email
            HashSet<String> espResponseKeys = new HashSet<String>();
            espResponseKeys.add("old_contact_name");
            espResponseKeys.add("old_contact_email");
            List<EspResponse> list = _espResponseDao.getResponsesByKeys(school, espResponseKeys, true);
            if(list != null) {
                for(EspResponse r : list) {
                    if("old_contact_name".equals(r.getKey())) mrow.setContactName(r.getSafeValue());
                    if("old_contact_email".equals(r.getKey())) mrow.setContactEmail(r.getSafeValue());
                }
            }

            List<EspMembership> otherActiveMemberships = getEspMembershipDao().findEspMembershipsByUserId(membership.getUser().getId(), true);
            if (otherActiveMemberships != null && !otherActiveMemberships.isEmpty()) {
                mrow.setHasOtherActiveEspMemberships(true);
            }

            List<EspMembership> activeMembershipsForTheSchool = getEspMembershipDao().findEspMembershipsBySchool(membership.getSchool(), true);
            if(activeMembershipsForTheSchool != null && activeMembershipsForTheSchool.size() > 0) {
                mrow.setSchoolHasActiveMemberships(true);
            }

            if(membership.getStatus().equals(EspMembershipStatus.PROVISIONAL) && !membership.getActive()){
                mrow.setUserHasProvisionalAccess(true);
            }

            // is disabled user re-requesting access?
            for(EspMembership rejected : rejectedMemberships) {
                try {
                    long membershipSchoolId = membership.getSchoolId() == null ? -1 : membership.getSchoolId().longValue();
                    long rejectedSchoolId = rejected.getSchoolId() == null ? -1 : rejected.getSchoolId().longValue();
                    long membershipUserId = membership.getUser().getId().longValue();
                    long rejectedUserId = rejected.getUser().getId().longValue();
                    if(membershipSchoolId == rejectedSchoolId && membershipUserId == rejectedUserId) {
                        mrow.setDisabledUserReRequestingAccess(true);
                        break;
                    }
                }
                catch(NullPointerException e) {
                    continue;
                }
            }
        }

        modelMap.put("mrows", mrows);
        //modelMap.put("approvedMemberships", approvedMemberships);
        //modelMap.put("rejectedMemberships", rejectedMemberships);
    }

    /**
     * Method to remove the ESP_MEMBER role when there are no active memberships present.
     *
     * @param user to remove the role
     */
    protected void removeEspRole(User user) {
        if (user != null) {
            boolean removeRole = true;
            List<EspMembership> espMemberships = getEspMembershipDao().findEspMembershipsByUserId(user.getId(), true);
            if (espMemberships != null && espMemberships.size() != 0) {
                for (EspMembership espMembership : espMemberships) {
                    if (espMembership.getStatus().equals(EspMembershipStatus.APPROVED)) {
                        removeRole = false;
                    }
                }
            }
            if (removeRole) {
                Role role = getRoleDao().findRoleByKey(Role.ESP_MEMBER);
                user.removeRole(role);
                getUserDao().updateUser(user);
            }
        }
    }


    /**
     * Method to add the ESP_MEMBER role if user does not already have one.
     *
     * @param user to add the role
     */
    protected void addEspRole(User user) {
        if (user != null && !user.hasRole(Role.ESP_MEMBER)) {
            Role role = getRoleDao().findRoleByKey(Role.ESP_MEMBER);
            user.addRole(role);
            getUserDao().updateUser(user);
        }
    }

    protected void sendESPApprovalEmail(User user, School school,HttpServletRequest request) {
        String redirect = new UrlBuilder(school, 6, UrlBuilder.SCHOOL_PROFILE_ESP_FORM).toString();
        sendEmail(user, school, redirect, "HTML__espFormUrl", "ESP-approval",request);
    }

    protected void sendESPVerificationEmail(User user, School school,HttpServletRequest request) {
        String redirect = new UrlBuilder(UrlBuilder.ESP_DASHBOARD).toString();
        sendEmail(user, school, redirect, "HTML__espVerificationUrl", "OSP-verification",request);
    }

    protected void sendEmail(User user, School school, String redirectUrl, String urlKey,
                             String ETKey,HttpServletRequest request) {
        try {
            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, 1);
            Date dateStamp = cal.getTime();
            String dateStampAsString = String.valueOf(dateStamp.getTime());
            hash = DigestUtil.hashString(hash + dateStampAsString);
            String redirect = redirectUrl;

            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.REGISTRATION_VALIDATION, null, hash + user.getId());
            urlBuilder.addParameter("date", dateStampAsString);
            urlBuilder.addParameter("redirect", redirect);

            StringBuffer espEmailUrl = new StringBuffer("<a href=\"");
            espEmailUrl.append(urlBuilder.asFullUrl(request));
            if (ETKey.equals("ESP-approval")) {
                espEmailUrl.append("\">" + "Click here" + "</a>");
            } else {
                espEmailUrl.append("\">" + urlBuilder.asFullUrl(request) + "</a>");
            }

            Map<String, String> emailAttributes = new HashMap<String, String>();
            emailAttributes.put(urlKey, espEmailUrl.toString());
            emailAttributes.put("first_name", user.getFirstName());
            if (school != null) {
                emailAttributes.put("school_name", school.getName());
            }
            getExactTargetAPI().sendTriggeredEmail(ETKey, user, emailAttributes);
        } catch (Exception e) {
            _log.error("Error sending verification email message: " + e, e);
        }
    }

    private void sendRejectionEmail(User user,School school) {
        try {
            if (user != null && StringUtils.isNotEmpty(user.getFirstName())) {
                Map<String, String> emailAttributes = new HashMap<String, String>();
                emailAttributes.put("first_name", user.getFirstName());
                if (school != null) {
                    emailAttributes.put("school_name", school.getName());
                }
                getExactTargetAPI().sendTriggeredEmail("OSP-rejection", user, emailAttributes);
            }
        } catch (Exception e) {
            _log.error("Error sending rejection email message: " + e, e);
        }

    }

    public IEspMembershipDao getEspMembershipDao() {
        return _espMembershipDao;
    }

    public void setEspMembershipDao(IEspMembershipDao espMembershipDao) {
        _espMembershipDao = espMembershipDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IRoleDao getRoleDao() {
        return _roleDao;
    }

    public void setRoleDao(IRoleDao roleDao) {
        _roleDao = roleDao;
    }

    public IEspResponseDao getEspResponseDao() {
        return _espResponseDao;
    }

    public void setEspResponseDao(IEspResponseDao espResponseDao) {
        this._espResponseDao = espResponseDao;
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI exactTargetAPI) {
        _exactTargetAPI = exactTargetAPI;
    }

    public void setEspSaveHelper(EspSaveHelper espSaveHelper) {
        _espSaveHelper = espSaveHelper;
    }

    public void setNoEditDao(INoEditDao noEditDao) {
        _noEditDao = noEditDao;
    }
}