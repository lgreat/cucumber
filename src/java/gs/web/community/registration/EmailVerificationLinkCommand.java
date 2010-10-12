package gs.web.community.registration;

import gs.data.community.User;
import gs.data.util.DigestUtil;


public class EmailVerificationLinkCommand {
    private String _hashPlusUserId;
    private String _dateSentAsString;
    private String _redirect;
    private User _user;

    public Integer getUserId() {
        if (getHashPlusUserId() == null || getHashPlusUserId().length() <= DigestUtil.MD5_HASH_LENGTH) {
            return null;
        }

        try {
            Integer id = Integer.parseInt(getHashPlusUserId().substring(DigestUtil.MD5_HASH_LENGTH));
            return id;
        } catch (NumberFormatException e) {
            return null; //cannot recover
        }
    }

    public String getHash() {
        if (getHashPlusUserId() == null || getHashPlusUserId().length() <= DigestUtil.MD5_HASH_LENGTH) {
            return null;
        }
        
        String hash = getHashPlusUserId().substring(0, DigestUtil.MD5_HASH_LENGTH);
        return hash;
    }

    public String getHashPlusUserId() {
        return _hashPlusUserId;
    }

    //why? because I wrote this command using hashPlusId as it's more descriptive, but email links that have already
    //been sent out use the parameter id, so we also need that setter for backward compatibility
    public void setHashPlusUserId(String id) {
        _hashPlusUserId = id;
    }
    public void setId(String id) {
        setHashPlusUserId(id);
    }

    /**
     * @return Date email verification link was sent, as a string 
     */
    public String getDateSentAsString() {
        return _dateSentAsString;
    }

    /**
     * @param dateSentAsString Date email verification link was sent, as a string
     */
    public void setDate(String dateSentAsString) {
        _dateSentAsString = dateSentAsString;
    }

    public String getRedirect() {
        return _redirect;
    }

    public void setRedirect(String redirect) {
        _redirect = redirect;
    }

    public User getUser() {
        return _user;
    }

    public void setUser(User user) {
        _user = user;
    }
}
