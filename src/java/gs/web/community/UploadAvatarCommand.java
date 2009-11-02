package gs.web.community;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class UploadAvatarCommand {
    private String _stockPhoto;
    private byte[] _avatar;

    public String getStockPhoto() {
        return _stockPhoto;
    }

    public void setStockPhoto(String stockPhoto) {
        _stockPhoto = stockPhoto;
    }

    public byte[] getAvatar() {
        return _avatar;
    }

    public void setAvatar(byte[] avatar) {
        _avatar = avatar;
    }
}
