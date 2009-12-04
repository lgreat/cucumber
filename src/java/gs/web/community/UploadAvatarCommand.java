package gs.web.community;

import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class UploadAvatarCommand {
    private String _stockPhoto;
    private MultipartFile avatar;
    private BufferedImage _image; // filled in by onBindAndValidate

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

    public BufferedImage getImage() {
        return _image;
    }

    public void setImage(BufferedImage image) {
        _image = image;
    }
}
