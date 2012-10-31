package gs.web;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import gs.web.util.UrlUtil;
import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;

public class HTML2PDFViewResolver implements ViewResolver, URIResolver {
    @Autowired
    private ResourceLoader resourceLoader;

    private ViewResolver viewResolver;

    private static final Logger LOGGER = Logger.getLogger(HTML2PDFViewResolver.class);

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HHmmss");

    // Spring calls resolveViewName
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        // get the view that's resolved by the configured view resolver in pages-servlet
        final View pdfView = viewResolver.resolveViewName(viewName, locale);

        // new View is created and given to Spring. This view wraps the view above
        return new View() {
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
                pdfView.render(model, request, wrapper);

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
                response.setHeader("Cache-Control","private");
                response.setHeader("Vary", "Accept-Encoding");

                // ITextRenderer uses base href when resolving assets like images / css
                String baseHref = UrlUtil.buildHostAndPortString(request).toString();

                ITextRenderer pdfRenderer = buildITextRenderer(document, baseHref);

                // create the PDF
                OutputStream os = response.getOutputStream();
                pdfRenderer.createPDF(os);
                os.flush();
                os.close();
            }

            /**
             *
             * @param document the DOM
             * @param baseHref used for resolving assets like images / css
             * @return
             * @throws IOException
             * @throws DocumentException
             */
            public ITextRenderer buildITextRenderer(Document document, String baseHref) throws IOException, DocumentException {
                ITextRenderer iTextRenderer = new ITextRenderer();
                iTextRenderer.setDocument(document,baseHref);
                iTextRenderer.layout();
                return iTextRenderer;
            }

            public String getContentType() {
                return "application/pdf";
            }
        };
    }

    public Source resolve(String href, String base) throws TransformerException {
        try {
            return new StreamSource(resourceLoader.getResource(href).getFile());
        }
        catch (IOException e) {
            throw new TransformerException(e);
        }
    }

    public ViewResolver getViewResolver() {
        return viewResolver;
    }

    public void setViewResolver(ViewResolver viewResolver) {
        this.viewResolver = viewResolver;
    }

}
