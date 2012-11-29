package gs.web;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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

import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.*;
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

    // Spring calls resolveViewName
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        // get the view that's resolved by the configured view resolver in pages-servlet
        final View htmlView = viewResolver.resolveViewName(viewName, locale);
        PdfView pdfView = new PdfView(htmlView);
        return pdfView;
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
