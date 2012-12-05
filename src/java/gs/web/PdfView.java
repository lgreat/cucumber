package gs.web;


import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfWriter;
import gs.web.util.UrlUtil;
import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.springframework.web.servlet.View;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class PdfView implements View {
    View _viewToWrap;
    String[] _pdfToAppend; // path to PDFs on disk

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HHmmss");

    private static final Logger _logger = Logger.getLogger(PdfView.class);

    public PdfView(View viewToWrap) {
        _viewToWrap = viewToWrap;
    }

    public PdfView(View viewToWrap, String... pdfsToAppend) {
        this(viewToWrap);
        _pdfToAppend = pdfsToAppend;
    }

    public String getContentType() {
        return "application/pdf";
    }

    // Spring calls render(...);
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        final StringWriter htmlWriter = new StringWriter();

        // We need access to the html that the view will render. So create a HttpServletResponseWrapper
        // that will use our html StringWriter
        HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(response) {
            @Override
            public PrintWriter getWriter() throws IOException {
                return new PrintWriter(htmlWriter);
            }

            // remove jsessionid URL encoding. Using the existing filter didnt work
            public String encodeRedirectUrl(String url) {
                return url;
            }

            public String encodeRedirectURL(String url) {
                return url;
            }

            public String encodeUrl(String url) {
                return url;
            }

            public String encodeURL(String url) {
                return url;
            }

            public void addCookie(Cookie cookie) {
                return;
            }
        };

        // does the work of rendering the html and writing to our StringWriter
        _viewToWrap.render(model, request, wrapper);

        // parse the html into a Document
        String html = htmlWriter.toString();
        InputStream is = new ByteArrayInputStream(html.getBytes("UTF-8"));

        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(is);

        // set response headers
        response.setContentType("application/pdf");
        String outFileName = "GreatSchools_Chooser_" + DATE_FORMATTER.format(new Date()) + ".pdf";
        response.setHeader("Cache-control", "no-store");
        response.setHeader("Content-disposition", "inline; filename=\"" + outFileName + "\"");
        response.setHeader("Cache-Control", "private");
        response.setHeader("Vary", "Accept-Encoding");

        // ITextRenderer uses base href when resolving assets like images / css
        String baseHref = UrlUtil.buildHostAndPortString(request).toString();

        ITextRenderer pdfRenderer = buildITextRenderer(document, baseHref);

        // create the PDF
        OutputStream os = response.getOutputStream();
        pdfRenderer.createPDF(os, false);

        if (_pdfToAppend != null) {
            try {
                appendPdfs(pdfRenderer, _pdfToAppend);
            } catch (IOException e) {
                _logger.debug("Failed to read PDF to append to end of generated PDF: "+ _pdfToAppend, e);
            }
        }

        pdfRenderer.finishPDF();
        os.flush();
        os.close();
    }


    /**
     * @param document the DOM
     * @param baseHref used for resolving assets like images / css
     * @return
     * @throws IOException
     * @throws DocumentException
     */
    public ITextRenderer buildITextRenderer(Document document, String baseHref) throws IOException, DocumentException {
        ITextRenderer iTextRenderer = new ITextRenderer();
        iTextRenderer.setDocument(document, baseHref);
        iTextRenderer.layout();
        return iTextRenderer;
    }

    public static void appendPdfs(ITextRenderer iTextRenderer, String[] pathsToPdfs) throws DocumentException, IOException, com.lowagie.text.DocumentException {
        PdfWriter writer = iTextRenderer.getWriter();
        PdfContentByte cb = writer.getDirectContent();

        for (String pdfToAppend : pathsToPdfs) {
            com.itextpdf.text.pdf.PdfReader reader = new com.itextpdf.text.pdf.PdfReader(pdfToAppend);

            PdfImportedPage page = writer.getImportedPage(reader, 1);
            cb.getPdfDocument().newPage();

            cb.addTemplate(page, 0, 0);
        }

    }
}

