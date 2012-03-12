package gs.web.email;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: eddie
 * Date: Mar 24, 2010
 * Time: 2:21:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class UnsubscribeCommand {
    protected final Log _log = LogFactory.getLog(getClass());

    private int _userId;
    private String _email;
    private boolean _weeklyNl;
    private boolean _dailyNl;
    private boolean _mss;
    private boolean _partnerOffers;
    private String _unsubscribeReason;
    private String _otherReasonsText;
    private boolean _unsubscribedSuccess;
    private int _unsubscribeId;
    private Date _unsubscribeDateTime;

    public Date getUnsubscribeDateTime() {
        return _unsubscribeDateTime;
    }

    public void setUnsubscribeDateTime(Date unsubscribeDateTime) {
        _unsubscribeDateTime = unsubscribeDateTime;
    }

    public int getUnsubscribeId() {
        return _unsubscribeId;
    }

    public void setUnsubscribeId(int unsubscribeId) {
        _unsubscribeId = unsubscribeId;
    }

   public boolean getUnsubscribedSuccess() {
        return _unsubscribedSuccess;
    }

    public void setUnsubscribedSuccess(boolean unsubscribedSuccess) {
        _unsubscribedSuccess = unsubscribedSuccess;
    }

    public String getUnsubscribeReason() {
        return _unsubscribeReason;
    }

    public void setUnsubscribeReason(String unsubscribeReason) {
        this._unsubscribeReason = unsubscribeReason;
    }

   public String getOtherReasonsText() {
        return _otherReasonsText;
    }

    public void setOtherReasonsText(String otherReasonsText) {
        this._otherReasonsText = otherReasonsText;
    }

    public boolean getWeeklyNl() {
        return _weeklyNl;
    }

    public void setWeeklyNl(String weeklyNl) {
        if("1".equals(weeklyNl)){
            this._weeklyNl = true;
        }
    }

    public void setWeeklyNl(boolean weeklyNl) {
        _weeklyNl = weeklyNl;
    }

    public boolean getDailyNl() {
        return _dailyNl;
    }

    public void setDailyNl(String dailyNl) {
        if("1".equals(dailyNl)){
            this._dailyNl = true;
        }
    }

    public void setDailyNl(boolean dailyNl) {
        _dailyNl = dailyNl;
    }

    public boolean getMss() {
        return _mss;
    }

    public void setMss(String mss) {
        if("1".equals(mss)){
            this._mss = true;
        }
    }

    public void setMss(boolean mss) {
        _mss = mss;
    }

    public boolean getPartnerOffers() {
        return _partnerOffers;
    }

    public void setPartnerOffers(String partnerOffers) {
        if("1".equals(partnerOffers)){
            this._partnerOffers = true;
        }
    }

    public void setPartnerOffers(boolean partnerOffers) {
        _partnerOffers = partnerOffers;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        this._email = email;
    }
    public int getUserId() {
        return _userId;
    }

    public void setUserId(int userId) {
        this._userId = userId;
    }

    public boolean isAllUnchecked(){
        if(!this._dailyNl && !this._mss && !this._partnerOffers && !this._weeklyNl){
            return true;
        }
        return false;
    }

}