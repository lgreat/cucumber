package gs.web.admin;

import gs.data.community.IBannedIPDao;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.axis.utils.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;


@Controller
public class BannedIPController implements ReadWriteAnnotationController {

    @Autowired
    private IBannedIPDao _bannedIPDao;

    @RequestMapping("/admin/schoolReview/bannedIp/list.page")
    public String list(ModelMap model){
        model.addAttribute("bannedIpList", _bannedIPDao.findBannedIPs(IBannedIPDao.DEFAULT_DAYS_BANNED));
        model.addAttribute("millisPerDay", DateUtils.MILLIS_PER_DAY);
        return "admin/schoolReview/bannedIpList";
    }

    /**
     * Add a new ip address to the list of banned ip's.
     * This will make sure a ban does not yet exist for
     * the default number of days banned before it inserts
     * the row.
     * @param ip
     * @param reason
     * @return "success"
     */
    @RequestMapping( value = "/admin/schoolReview/bannedIp/add.page", method = RequestMethod.POST )
    public @ResponseBody String add( @RequestParam("ip") String ip,
                       @RequestParam(value = "reason", required = false) String reason ){

        // ban ip only if hasn't been banned before
        if ( !_bannedIPDao.isIPBanned( ip, IBannedIPDao.DEFAULT_DAYS_BANNED ) ) {
            _bannedIPDao.addIP( ip, StringUtils.isEmpty(reason) ? "Banned through admin page" : reason );
        }

        return "success";
    }

    /**
     * Delete a banned ip address from the database
     * POST /admin/schoolReview/bannedIp/delete.page
     * @param ip
     * @return redirect back to list page
     */
    @RequestMapping( value = "/admin/schoolReview/bannedIp/delete.page", method = RequestMethod.POST )
    public String delete(@RequestParam("ip") String ip){
        // remove the ban on a particular ip
        _bannedIPDao.removeIP(ip);
        return "redirect:/admin/schoolReview/bannedIp/list.page";
    }

    public IBannedIPDao getBannedIPDao() {
        return _bannedIPDao;
    }

    public void setBannedIPDao(IBannedIPDao bannedIPDao) {
        _bannedIPDao = bannedIPDao;
    }
}
