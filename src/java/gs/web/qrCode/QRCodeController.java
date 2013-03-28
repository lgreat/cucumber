package gs.web.qrCode;

import com.itextpdf.text.pdf.qrcode.ByteMatrix;
import com.itextpdf.text.pdf.qrcode.QRCodeWriter;
import com.itextpdf.text.pdf.qrcode.WriterException;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 1/22/13
 * Time: 2:38 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class QRCodeController {

    private QRCodeWriter _qrCodeWriter = new QRCodeWriter();
    private static Logger _logger = Logger.getLogger(QRCodeController.class);

    @RequestMapping(value = "/real-estate/qr-code-gen", method = RequestMethod.GET)
    public void getQRCodeImage(@RequestParam(value = "content", required = true) String content,
                               @RequestParam(value = "width", required = true) Integer width,
                               @RequestParam(value = "height", required = true) Integer height,
                               HttpServletRequest request,
                               HttpServletResponse response) throws QRCodeException{

//        content = "http://localhost:8080/search/search.page?lat=38.8951118&lon=-77.0363658&state=DC";
//        width = 150;
//        height = 150;
        try {
            content = URLDecoder.decode(content, "UTF-8");
            ByteMatrix byteMatrix = _qrCodeWriter.encode(content, width, height);
            byte[][] array = byteMatrix.getArray();

            int matrixWidth = byteMatrix.getWidth();
            int matrixHeight = byteMatrix.getHeight();
            BufferedImage image = new BufferedImage(matrixWidth, matrixHeight, BufferedImage.TYPE_INT_RGB);
            for(int h = 0; h < matrixHeight; h++) {
                for(int w = 0; w < matrixWidth; w++) {
                    int grayValue = array[h][w] & 0xff;
                    image.setRGB(w, h, (grayValue == 0 ? 0 : 0xFFFFFF));
                }
            }

            response.setContentType("image/png");
            response.setHeader("Content-disposition", "inline");
            response.setHeader("Cache-Control", "no-cache");

            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
            os.close();
        }
        catch (UnsupportedEncodingException e) {
            _logger.debug("QRCodeController - Exception while trying to decode location search url param. " + e);
            throw new QRCodeException("QRCodeController - Exception while trying to decode location search url param.", e.getCause());
        }
        catch (WriterException e) {
            _logger.debug("QRCodeController - Exception while trying to generate QR code byte matrix. " + e);
            throw new QRCodeException("QRCodeController - Exception while trying to generate QR code byte matrix.", e.getCause());
        }
        catch (IOException e) {
            _logger.debug("QRCodeController - Exception while trying to write the QR Code image to output stream. " + e);
            throw new QRCodeException("QRCodeController - Exception while trying to write the QR Code image to output stream.", e.getCause());
        }
        catch (Exception e) {
            _logger.debug("QRCodeController - Exception while trying to get the QR Code image." + e);
            throw new QRCodeException("QRCodeController - Exception while trying to get the QR Code image.", e.getCause());
        }
    }

    @ExceptionHandler(Exception.class)
    private void handleException (Exception e) {
        _logger.debug("QRCodeController - Exception while trying to process the request." + e);
    }

    protected class QRCodeException extends Exception {
        public QRCodeException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
