package gs.web.about;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

/**
 * Streams out a requested GreatSchools logo image based on requested size.
 * This is only needed because the download link must force a download instead of showing
 * the image directly in the browser.
 * @author Young Fan
 */
public class DownloadLogoController implements Controller {

    final static String PARAM_SIZE = "size";
    final static String PARAM_VAL_SMALL = "small";
    final static String PARAM_VAL_MEDIUM = "medium";
    final static String PARAM_VAL_LARGE = "large";
    final static String IMAGE_PATH = "/res/img/logo/";
    final static String SMALL_FILE_NAME = "logo_GS_201x38.gif";
    final static String MEDIUM_FILE_NAME = "logo_GS_233x46.gif";
    final static String LARGE_FILE_NAME = "logo_GS_304x58.gif";

    public String chooseFileName(String size) {
        if (PARAM_VAL_SMALL.equals(size)) {
            return SMALL_FILE_NAME;
        } else if (PARAM_VAL_MEDIUM.equals(size)) {
            return MEDIUM_FILE_NAME;
        } else {
            return LARGE_FILE_NAME;
        }
    }

    public ModelAndView handleRequest(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        // determine which size logo was requested
        String size = request.getParameter(PARAM_SIZE);

        // set the file name and file path
        String fileName = chooseFileName(size);
        String filePath = IMAGE_PATH + fileName;

        // set response headers
        response.setContentType("image/gif");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // build the url for the file
        URL url = new URL(request.getScheme() + "://" + request.getServerName() +
            ":" + request.getServerPort() + filePath);
        URLConnection connection = url.openConnection();

        // open the input and output streams
        BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
        BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());

        // stream out the file
        int i;
        while ((i = in.read()) != -1) {
            out.write(i);
        }

        out.flush();
        out.close();
        in.close();
        
        return null;
    }
}
