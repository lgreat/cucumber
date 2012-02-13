package gs.web.admin;

import static org.apache.commons.lang.StringUtils.*;
import gs.data.school.EspMembership;
import gs.data.school.EspMembershipStatus;

import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/admin/espModerationForm.page")
public class EspModerationController extends AbstractEspModerationController {
    
    public static final String VIEW = "admin/espModerationForm";

    @Override
    protected String getViewName() {
        return VIEW;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String display(ModelMap modelMap, HttpServletRequest request) {
        EspModerationCommand command = new EspModerationCommand();
        modelMap.addAttribute("espModerationCommand", command);
        modelMap.put("isPendingView", "1".equals(request.getParameter("pending")));
        List<EspMembership> memberships = getEspMembershipDao().findAllEspMemberships(false);
        populateModelWithMemberships(memberships, modelMap);
        return getViewName();
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public String handlePost(@ModelAttribute("espModerationCommand") EspModerationCommand command,
                                      BindingResult result,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        boolean isPendingView = "1".equals(request.getParameter("pending"));
        updateEspMembership(command, request, response);
        return String.format("redirect:/%s.page?pending=%s", getViewName(), isPendingView? "1" : "0");
    }

    @Override
    protected void filterMembershipRows(List<EspMembership> memberships, ModelMap modelMap) {
        boolean isPendingView = Boolean.TRUE == modelMap.get("isPendingView");
        HashSet<EspMembership> toRemove = new HashSet<EspMembership>(memberships.size());
        for(EspMembership m : memberships) {
            if(m.getStatus() != EspMembershipStatus.PROCESSING) 
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
} 