package gs.web.admin;

import gs.data.community.BannedIP;
import gs.data.community.IBannedIPDao;
import gs.web.BaseControllerTestCase;
import org.springframework.ui.ModelMap;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;


/**
 * Created with IntelliJ IDEA.
 * User: cauer
 * Date: 4/3/12
 * Time: 1:57 PM
 */
public class BannedIPControllerTest extends BaseControllerTestCase {

    BannedIPController _bannedIPController;
    IBannedIPDao _bannedIPDao;

    public void setUp() throws Exception {
        super.setUp();
        _bannedIPController = new BannedIPController();
        _bannedIPDao = createStrictMock(IBannedIPDao.class);
        _bannedIPController.setBannedIPDao(_bannedIPDao);
    }

    /**
     * Make sure that the list controller returns correct
     * values to build the list view
     */
    public void testList() {
        ModelMap map = new ModelMap();
        expect(_bannedIPDao.findBannedIPs(eq(IBannedIPDao.DEFAULT_DAYS_BANNED))).andReturn(bannedIps());
        replay(_bannedIPDao);
        String result = _bannedIPController.list(map);
        assertEquals("Wrong view was returned from controller", "admin/schoolReview/bannedIpList", result);
        assertTrue(map.containsAttribute("bannedIpList"));
        assertTrue(map.containsAttribute("millisPerDay"));
        assertEquals("Wrong size of list for some reason", 1, ((ArrayList<BannedIP>)map.get("bannedIpList")).size());
        verify(_bannedIPDao);
    }

    /**
     * Expects that the requested ip address is passed through to
     * the DAO call to removeIP
     */
    public void testDelete() {
        // make sure that IP is passed through successfully
        _bannedIPDao.removeIP(eq("1.2.3.4"));
        replay(_bannedIPDao);
        _bannedIPController.delete("1.2.3.4");
        verify(_bannedIPDao);
    }

    /**
     * Test to make sure that add method doesn't
     * insert a duplicate row into the database when
     * the IP address is already blocked
     */
    public void testAddWithIpAlreadyBlocked() {
        expect(_bannedIPDao.isIPBanned(eq("1.2.3.4"), eq(IBannedIPDao.DEFAULT_DAYS_BANNED))).andReturn(true);
        replay(_bannedIPDao);
        String result = _bannedIPController.add("1.2.3.4", "reason");
        assertEquals("success", result);
        verify(_bannedIPDao);
    }

    /**
     * Test to make sure that the ip address is
     * added to the database when it is not currently
     * being blocked
     */
    public void testAddWithoutIpAlreadyBlocked() {
        expect(_bannedIPDao.isIPBanned(eq("1.2.3.4"), eq(IBannedIPDao.DEFAULT_DAYS_BANNED))).andReturn(false);
        _bannedIPDao.addIP(eq("1.2.3.4"), eq("reason"));
        replay(_bannedIPDao);
        String result = _bannedIPController.add("1.2.3.4", "reason");
        assertEquals("success", result);
        verify(_bannedIPDao);
    }

    /**
     * Test to make sure that adding an
     * ip address still works even when there
     * is no reason.
     */
    public void testAddWithoutReason(){
        expect(_bannedIPDao.isIPBanned(eq("1.2.3.4"), eq(IBannedIPDao.DEFAULT_DAYS_BANNED))).andReturn(false);
        _bannedIPDao.addIP(eq("1.2.3.4"), eq("Banned through admin page"));
        replay(_bannedIPDao);
        String result = _bannedIPController.add("1.2.3.4", null);
        assertEquals("success", result);
        verify(_bannedIPDao);
    }

    /**
     * provide a common list of bannedIps
     * @return
     */
    private List<BannedIP> bannedIps(){
        List<BannedIP> list = new ArrayList<BannedIP>();

        BannedIP ip = new BannedIP();
        ip.setIp("127.0.0.1");
        ip.setId(new Long(1));
        ip.setReason("reason");
        list.add(ip);

        return list;
    }

}
