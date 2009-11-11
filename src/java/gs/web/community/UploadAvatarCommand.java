package gs.web.community;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class UploadAvatarCommand {
    private String _stockPhoto;
    private MultipartFile avatar;

    public MultipartFile getAvatar() {
        return avatar;
    }

    public void setAvatar(MultipartFile avatar) {
        this.avatar = avatar;
    }

    public String getStockPhoto() {
        return _stockPhoto;
    }

    public void setStockPhoto(String stockPhoto) {
        _stockPhoto = stockPhoto;
    }
}
