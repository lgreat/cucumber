//package gs.web.servlet.filter;
//
//import javax.servlet.*;
//import javax.servlet.http.HttpServletResponse;
//import javax.xml.transform.Source;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.stream.StreamResult;
//import javax.xml.transform.stream.StreamSource;
//import java.io.*;
//
//public class XsltFilter implements Filter {
//    private FilterConfig filterConfig = null;
//
//    public void init(FilterConfig filterConfig) throws
//            ServletException {
//        this.filterConfig = filterConfig;
//    }
//
//    public void destroy() {
//        this.filterConfig = null;
//    }
//
//    public void doFilter(ServletRequest request, ServletResponse
//            response, FilterChain chain) throws IOException, ServletException {
////		String contentType = Consts.HTML_CONTENT;
//        String styleSheet;
//
//        System.out.println("Attempting to find filename paramter");
//        System.out.println("request.getParameter(filename): "
//                + request.getParameter(Consts.FILENAME_ATTR));
//
//        // Locate XSLT Stylesheet
//        styleSheet = Consts.BASEPATH +
//
//                request.getParameter(Consts.FILENAME_ATTR) +
//                Consts.XSLT_EXTENSION;
//
//        System.out.println("Here is the stylesheet we are going to load: " + styleSheet);
//
//        response.setContentType(contentType);
//        String stylePath =
//                filterConfig.getServletContext().getRealPath(styleSheet);
//        File styleFile = new File(stylePath);
//
//        if (!styleFile.exists()) {
//            System.out.println("File does not exist");
//        } else {
//            System.out.println("File exists!");
//        }
//
//        System.out.println("Retrieving stylesheet Real Path: "
//                + stylePath);
//        Source styleSource = new StreamSource(styleFile);
//
//        PrintWriter out = response.getWriter();
//        CharResponseWrapper wrapper = new
//                CharResponseWrapper((HttpServletResponse) response);
//        chain.doFilter(request, wrapper);
//
//        // Get response from Servlet
//        StringReader sr = new
//                StringReader(wrapper.toString());
//        Source xmlSource = new StreamSource((Reader) sr);
//
//        // Ok... Transform the xml:
//        try {
//            TransformerFactory transformerFactory =
//                    TransformerFactory.newInstance();
//
//            Transformer transformer =
//                    transformerFactory.newTransformer(styleSource);
//            CharArrayWriter caw = new CharArrayWriter();
//            StreamResult result = new StreamResult(caw);
//            transformer.transform(xmlSource, result);
//
//            response.setContentLength(caw.toString().length());
//            out.write(caw.toString());
//        } catch (Exception ex) {
//            out.println(ex.toString());
//            out.write(wrapper.toString());
//        }
//    }
//
//}