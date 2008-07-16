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
    public ModelAndView handleRequest(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        // determine which size logo was requested
        String size = request.getParameter("size");

        // set the file name and file path
        StringBuilder filePath = new StringBuilder("/res/img/logo/");
        String fileName;
        if ("small".equals(size)) {
            fileName = "logo_GS_201x38.gif";
            filePath.append(fileName);
        } else if ("medium".equals(size)) {
            fileName = "logo_GS_233x46.gif";
            filePath.append(fileName);
        } else {
            fileName = "logo_GS_304x58.gif";
            filePath.append(fileName);
        }

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
