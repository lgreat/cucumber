package gs.web.cbi;
import gs.web.util.UrlUtil;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;
import java.net.URL;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.web.servlet.mvc.Controller;

//Using external dependencies in grails1.1 without adding an additional jar
//did not work with both maven and ant.Therefore using gsweb to add the external dependency.
//Ideally this should be moved to CB once we upgrade to grails1.3.

public class GraduationCertificateController implements Controller {
    final static String CERTIFICATE_PATH = "/library/cbi/diplomas/";
    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {

        String name = request.getParameter("name");
        String lang = request.getParameter("lang");
        String level = request.getParameter("level");
        if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(lang) && !StringUtils.isEmpty(level)) {
//            PDDocument document = PDDocument.load("c:/certificate_1_en.pdf");
            String certificateName = "certificate_"+level+"_"+lang+".pdf";
            String certificatePath = CERTIFICATE_PATH + certificateName;
            URL url;
            if(UrlUtil.isDeveloperWorkstation(request.getServerName())){
                url = new URL("http://staging.greatschools.org/library/cbi/diplomas/"+certificateName);
            }else{
                url = new URL(request.getScheme() + "://" + request.getServerName() +
                    ":" + request.getServerPort() + certificatePath);
            }
            PDDocument document = PDDocument.load(url);

            List allPages = document.getDocumentCatalog().getAllPages();
            PDFont font = PDType1Font.TIMES_ROMAN;
            float fontSize = 26.0f;

            PDPage page = (PDPage) allPages.get(0);
            float stringWidth = font.getStringWidth(name);
            PDRectangle pageSize = page.findMediaBox();
            float centeredPosition = (pageSize.getWidth() - ((stringWidth * fontSize) / 1000f)) / 2f;

            PDPageContentStream contentStream = new PDPageContentStream(document, page, true, false);

            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.moveTextPositionByAmount((float) centeredPosition, 255f);
            contentStream.setNonStrokingColor(17,42,111);
            contentStream.drawString(name);
            contentStream.endText();
            contentStream.close();

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=collegebound_grad.pdf");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "No-cache");
            document.save(response.getOutputStream());
            document.close();
        }
        return null;

    }

}