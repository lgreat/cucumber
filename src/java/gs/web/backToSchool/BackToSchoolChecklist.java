package gs.web.backToSchool;

import gs.data.community.IUserDao;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.data.community.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;


public class BackToSchoolChecklist {

    @Autowired
    private IUserDao _userDao;

    public enum BackToSchoolChecklistItem {
        JOIN,
        EMAIL_SERIES,
        BACK_TO_SCHOOL_SUPPLY_LIST,
        ARTICLE1,
        ARTICLE2;

        /**
         * Return a byte representing the enum item's position.
         * @return
         */
        public byte byteValue() {
            return (byte) Math.pow(2d, this.ordinal());
        }
    }

    public static final BackToSchoolChecklistItem[] solutionSet = {BackToSchoolChecklistItem.ARTICLE1,BackToSchoolChecklistItem.ARTICLE2,BackToSchoolChecklistItem.BACK_TO_SCHOOL_SUPPLY_LIST};

    public static final boolean hasCompletedChecklist(User user) {
        List<BackToSchoolChecklistItem> completedItems = getCompletedItems(user);

        return (completedItems.containsAll(Arrays.asList(solutionSet)) && hasCompletedEmailSeries(user));
    }

    public static final int getNumberOfCompletedItems(User user) {
        return getCompletedItems(user).size();
    }

    public static final List<BackToSchoolChecklistItem> getCompletedItems(User user) {
        List<BackToSchoolChecklistItem> completedItems = new ArrayList<BackToSchoolChecklist.BackToSchoolChecklistItem>();

        UserProfile profile = null;
        
        if (user != null && user.getUserProfile() != null) {
            profile = user.getUserProfile();
            completedItems.add(BackToSchoolChecklistItem.JOIN);
        }

        if (profile != null) {
            Integer iCompletedItems = profile.getBackToSchoolChecklist();

            if (iCompletedItems != null) {
                for (BackToSchoolChecklist.BackToSchoolChecklistItem item : BackToSchoolChecklist.BackToSchoolChecklistItem.values()) {
                    if ((item.byteValue() & iCompletedItems.byteValue()) == item.byteValue()) {
                        completedItems.add(item);
                    }
                }
            }
        }

        if (hasCompletedEmailSeries(user)){
            completedItems.add(BackToSchoolChecklistItem.EMAIL_SERIES);
        }

        return completedItems;
    }

    
    public static final boolean hasCompletedEmailSeries(User user) {

        if (user != null) {
            if (user.findSubscription(SubscriptionProduct.BTSTIP_E) != null || user.findSubscription(SubscriptionProduct.BTSTIP_M) != null || user.findSubscription(SubscriptionProduct.BTSTIP_H) != null){
                return true;
            }
        }
        
        return false;
    }

    public boolean addChecklistItem(String checklistItem, User user) {
        boolean added = false;
        UserProfile profile = null;
        if (user != null && user.getUserProfile() != null) {
            profile = user.getUserProfile();
        }

        BackToSchoolChecklist.BackToSchoolChecklistItem completedItem = BackToSchoolChecklist.BackToSchoolChecklistItem.valueOf(checklistItem);

        if (completedItem != null && profile != null) {
            if (profile.getBackToSchoolChecklist() == null) {
                profile.setBackToSchoolChecklist((int)completedItem.byteValue());
            } else {
                profile.setBackToSchoolChecklist(profile.getBackToSchoolChecklist().byteValue() | completedItem.byteValue());
            }
            _userDao.updateUser(user);
            added = true;
        }

        return added;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
