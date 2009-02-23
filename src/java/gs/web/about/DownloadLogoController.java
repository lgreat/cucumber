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
    final static String SMALL_FILE_NAME = "logo_GS_200x50.gif";
    final static String MEDIUM_FILE_NAME = "logo_GS_233x46.gif";
    final static String LARGE_FILE_NAME = "logo_GS_304x58.gif";

    final static String IMAGE_PATH_BADGE = "/res/img/school/topSchools/badge/";
    final static String PARAM_BADGE_TYPE_NAME = "badgeTypeAndName";

    final static String PARAM_VAL_TOP_PERFORMING_HIGH_RES_JPEG = "topPerformingHighResJpg";
    final static String PARAM_VAL_TOP_PERFORMING_LOW_RES_JPEG = "topPerformingLowResJpg";
    final static String PARAM_VAL_TOP_PERFORMING_HIGH_RES_PNG = "topPerformingHighResPng";
    final static String PARAM_VAL_TOP_PERFORMING_LOW_RES_PNG = "topPerformingLowResPng";
    final static String PARAM_VAL_LOW_INCOME_HIGH_RES_JPEG = "lowIncomeHighResJpg";
    final static String PARAM_VAL_LOW_INCOME_LOW_RES_JPEG = "lowIncomeLowResJpg";
    final static String PARAM_VAL_LOW_INCOME_HIGH_RES_PNG = "lowIncomeHighResPng";
    final static String PARAM_VAL_LOW_INCOME_LOW_RES_PNG = "lowIncomeLowResPng";
    final static String PARAM_VAL_MOST_IMPROV_HIGH_RES_JPEG = "mostImprovedHighResJpg";
    final static String PARAM_VAL_MOST_IMPROV_LOW_RES_JPEG = "mostImprovedLowResJpg";
    final static String PARAM_VAL_MOST_IMPROV_HIGH_RES_PNG = "mostImprovedHighResPng";
    final static String PARAM_VAL_MOST_IMPROV_LOW_RES_PNG = "mostImprovedLowResPng";
    final static String PARAM_VAL_PUBLIC_SCHOOL_HIGH_RES_JPEG = "publicSchoolHighResJpg";
    final static String PARAM_VAL_PUBLIC_SCHOOL_LOW_RES_JPEG = "publicSchoolLowResJpg";
    final static String PARAM_VAL_PUBLIC_SCHOOL_HIGH_RES_PNG = "publicSchoolHighResPng";
    final static String PARAM_VAL_PUBLIC_SCHOOL_LOW_RES_PNG = "publicSchoolLowResPng";
    final static String PARAM_VAL_PRIVATE_SCHOOL_HIGH_RES_JPEG = "privateSchoolHighResJpg";
    final static String PARAM_VAL_PRIVATE_SCHOOL_LOW_RES_JPEG = "privateSchoolLowResJpg";
    final static String PARAM_VAL_PRIVATE_SCHOOL_HIGH_RES_PNG = "privateSchoolHighResPng";
    final static String PARAM_VAL_PRIVATE_SCHOOL_LOW_RES_PNG = "privateSchoolLowResPng";

    final static String TOP_PERFORMING_HIGH_RES_JPEG = "smbadge_highres_topperf.jpg";
    final static String TOP_PERFORMING_LOW_RES_JPEG = "smbadge_lowres_topperf.jpg";
    final static String TOP_PERFORMING_HIGH_RES_PNG = "smbadge_highres_topperf.png";
    final static String TOP_PERFORMING_LOW_RES_PNG = "smbadge_lowres_topperf.png";

    final static String LOW_INCOME_HIGH_RES_JPEG = "smbadge_highres_topperf_lowin.jpg";
    final static String LOW_INCOME_LOW_RES_JPEG = "smbadge_lowres_topperf_lowin.jpg";
    final static String LOW_INCOME_HIGH_RES_PNG = "smbadge_highres_topperf_lowin.png";
    final static String LOW_INCOME_LOW_RES_PNG = "smbadge_lowres_topperf_lowin.png";

    final static String MOST_IMPROV_HIGH_RES_JPEG = "smbadge_highres_improved.jpg";
    final static String MOST_IMPROV_LOW_RES_JPEG = "smbadge_lowres_improved.jpg";
    final static String MOST_IMPROV_HIGH_RES_PNG = "smbadge_highres_improved.png";
    final static String MOST_IMPROV_LOW_RES_PNG = "smbadge_lowres_improved.png";

    final static String PUBLIC_SCHOOL_HIGH_RES_JPEG = "smbadge_highres_pchoicepublic.jpg";
    final static String PUBLIC_SCHOOL_LOW_RES_JPEG = "smbadge_lowres_pchoicepublic.jpg";
    final static String PUBLIC_SCHOOL_HIGH_RES_PNG = "smbadge_highres_pchoicepublic.png";
    final static String PUBLIC_SCHOOL_LOW_RES_PNG = "smbadge_lowres_pchoicepublic.png";

    final static String PRIVATE_SCHOOL_HIGH_RES_JPEG = "smbadge_highres_pchoiceprivate.jpg";
    final static String PRIVATE_SCHOOL_LOW_RES_JPEG = "smbadge_lowres_pchoiceprivate.jpg";
    final static String PRIVATE_SCHOOL_HIGH_RES_PNG = "smbadge_highres_pchoiceprivate.png";
    final static String PRIVATE_SCHOOL_LOW_RES_PNG = "smbadge_lowres_pchoiceprivate.png";


    public String chooseFileName(String size) {
        if (PARAM_VAL_SMALL.equals(size)) {
            return SMALL_FILE_NAME;
        } else if (PARAM_VAL_MEDIUM.equals(size)) {
            return MEDIUM_FILE_NAME;
        } else {
            return LARGE_FILE_NAME;
        }
    }

    public String chooseBadge(String typeAndName){

         if(PARAM_VAL_TOP_PERFORMING_HIGH_RES_JPEG.equals(typeAndName)){
             return TOP_PERFORMING_HIGH_RES_JPEG;
         }else if(PARAM_VAL_TOP_PERFORMING_LOW_RES_JPEG.equals(typeAndName)){
             return TOP_PERFORMING_LOW_RES_JPEG;
         }else if(PARAM_VAL_TOP_PERFORMING_HIGH_RES_PNG.equals(typeAndName)){
             return TOP_PERFORMING_HIGH_RES_PNG;
         }else if(PARAM_VAL_TOP_PERFORMING_LOW_RES_PNG.equals(typeAndName)){
             return TOP_PERFORMING_LOW_RES_PNG;
         }else if(PARAM_VAL_LOW_INCOME_HIGH_RES_JPEG.equals(typeAndName)){
             return LOW_INCOME_HIGH_RES_JPEG;
         }else if(PARAM_VAL_LOW_INCOME_LOW_RES_JPEG.equals(typeAndName)){
             return LOW_INCOME_LOW_RES_JPEG;
         }else if(PARAM_VAL_LOW_INCOME_HIGH_RES_PNG.equals(typeAndName)){
             return LOW_INCOME_HIGH_RES_PNG;
         }else if(PARAM_VAL_LOW_INCOME_LOW_RES_PNG.equals(typeAndName)){
             return LOW_INCOME_LOW_RES_PNG;
         }else if(PARAM_VAL_MOST_IMPROV_HIGH_RES_JPEG.equals(typeAndName)){
             return MOST_IMPROV_HIGH_RES_JPEG;
         }else if(PARAM_VAL_MOST_IMPROV_LOW_RES_JPEG.equals(typeAndName)){
             return MOST_IMPROV_LOW_RES_JPEG;
         }else if(PARAM_VAL_MOST_IMPROV_HIGH_RES_PNG.equals(typeAndName)){
             return MOST_IMPROV_HIGH_RES_PNG;
         }else if(PARAM_VAL_MOST_IMPROV_LOW_RES_PNG.equals(typeAndName)){
             return MOST_IMPROV_LOW_RES_PNG;
         }else if(PARAM_VAL_PUBLIC_SCHOOL_HIGH_RES_JPEG.equals(typeAndName)){
             return PUBLIC_SCHOOL_HIGH_RES_JPEG;
         }else if(PARAM_VAL_PUBLIC_SCHOOL_LOW_RES_JPEG.equals(typeAndName)){
             return PUBLIC_SCHOOL_LOW_RES_JPEG;
         }else if(PARAM_VAL_PUBLIC_SCHOOL_HIGH_RES_PNG.equals(typeAndName)){
             return PUBLIC_SCHOOL_HIGH_RES_PNG;
         }else if(PARAM_VAL_PUBLIC_SCHOOL_LOW_RES_PNG.equals(typeAndName)){
             return PUBLIC_SCHOOL_LOW_RES_PNG;
         }else if(PARAM_VAL_PRIVATE_SCHOOL_HIGH_RES_JPEG.equals(typeAndName)){
             return PRIVATE_SCHOOL_HIGH_RES_JPEG;
         }else if(PARAM_VAL_PRIVATE_SCHOOL_LOW_RES_JPEG.equals(typeAndName)){
             return PRIVATE_SCHOOL_LOW_RES_JPEG;
         }else if(PARAM_VAL_PRIVATE_SCHOOL_HIGH_RES_PNG.equals(typeAndName)){
             return PRIVATE_SCHOOL_HIGH_RES_PNG;
         }else{
             return PRIVATE_SCHOOL_LOW_RES_PNG;
         }
    }

    public ModelAndView handleRequest(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {            
        // determine which size logo was requested
        String size = request.getParameter(PARAM_SIZE);
        String fileName = "";
        String filePath = "";

        //size parameter is passed only for the download of GreatSchools Logo and
        // not for the downloads of Top Schools Bagdes.
        if(size != null){
            // set the file name and file path
            fileName = chooseFileName(size);
            filePath = IMAGE_PATH + fileName;
        }else{
           String type = request.getParameter(PARAM_BADGE_TYPE_NAME);
           fileName = chooseBadge(type);
           filePath = IMAGE_PATH_BADGE +fileName;
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
