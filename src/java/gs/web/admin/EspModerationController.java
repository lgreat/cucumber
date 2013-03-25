package gs.web.admin;

import static org.apache.commons.lang.StringUtils.*;

import gs.data.json.JSONObject;
import gs.data.school.EspMembership;
import gs.data.school.EspMembershipStatus;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.state.INoEditDao;
import gs.web.util.HttpCacheInterceptor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/")
public class EspModerationController extends AbstractEspModerationController {
    
    public static final String VIEW = "admin/espModerationForm";

    HttpCacheInterceptor _cacheInterceptor = new HttpCacheInterceptor();

    @Override
    protected String getViewName() {
        return VIEW;
    }

    @Autowired
    private INoEditDao _noEditDao;

    @RequestMapping(value = "espModerationForm.page", method = RequestMethod.GET)
    public String display(ModelMap modelMap, HttpServletRequest request) {
        EspModerationCommand command = new EspModerationCommand();
        modelMap.addAttribute("espModerationCommand", command);
        modelMap.put("isPendingView", "1".equals(request.getParameter("pending")));
        List<EspMembership> memberships = getEspMembershipDao().findAllEspMemberships(false);
        populateModelWithMemberships(memberships, modelMap);
        return getViewName();
    }
    
    @RequestMapping(value = "espModerationForm.page", method = RequestMethod.POST)
    public String handlePost(@ModelAttribute("espModerationCommand") EspModerationCommand command,
                                      BindingResult result,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        boolean isPendingView = "1".equals(request.getParameter("pending"));
        updateEspMembership(command, request, response);
        return String.format("redirect:/%s.page?pending=%s", getViewName(), isPendingView ? "1" : "0");
    }

    @Override
    protected void filterMembershipRows(List<EspMembership> memberships, ModelMap modelMap) {
        boolean isPendingView = Boolean.TRUE == modelMap.get("isPendingView");
        HashSet<EspMembership> toRemove = new HashSet<EspMembership>(memberships.size());
        for(EspMembership m : memberships) {
            if(m.getStatus() != EspMembershipStatus.PROCESSING && m.getStatus() != EspMembershipStatus.PROVISIONAL)
                toRemove.add(m);
            else {
                if(isPendingView) {
                    // "pending" - display entries w/ non-empty notes 
                    if(isEmpty(m.getNote())) toRemove.add(m);  
                } else {
                    // "main" - display entries w/ empty notes 
                    if(isNotEmpty(m.getNote())) toRemove.add(m);                      
                }
            }
        }
        for(EspMembership m : toRemove) {
            memberships.remove(m);
        }
    }

    @RequestMapping(value = "espModerationForm/checkStateLocks.page", method = RequestMethod.GET)
    protected void checkStateLockedForProvisionalEspMemberships(@RequestParam(value = "memberIds", required = true) String memberIds
            , HttpServletResponse response) {
        List<String> membershipIdsList = Arrays.asList(memberIds.split(","));
        List<Integer> membershipIdsWithStateLocked = new ArrayList<Integer>();
        for (String memberIdStr : membershipIdsList) {
            try {
                Integer membershipId = Integer.parseInt(memberIdStr);
                EspMembership membership = _espMembershipDao.findEspMembershipById(membershipId, false);

                if (membership != null && _noEditDao.isStateLocked(membership.getState())) {
                    membershipIdsWithStateLocked.add(membershipId);
                }

            } catch (NumberFormatException ex) {
                _log.error("Error parsing member id:" + memberIdStr);
                continue;
            }
        }

        if (!membershipIdsWithStateLocked.isEmpty()) {
            try {
                Map data = new HashMap();
                data.put("idsWithStateLocked", StringUtils.join(membershipIdsWithStateLocked.toArray(), ","));
                JSONObject rval = new JSONObject(data);
                _cacheInterceptor.setNoCacheHeaders(response);
                response.setContentType("application/json");
                response.getWriter().print(rval.toString());
                response.getWriter().flush();
            } catch (Exception exp) {
                _log.error("Error " + exp, exp);
            }
        }
    }
} 