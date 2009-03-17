package gs.web.api;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Source;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author chriskimm@greatschools.net
 */
public class ReportTagHandler extends SimpleTagSupport {

    private String _reportUrl;
    private HttpClient _httpClient;
    private GetMethod _method;

    String xslt =
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            "<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>\n" +
            "    <xsl:output method='html' version='1.0' encoding='utf-8' indent='no'/>\n" +
            "    <xsl:output indent=\"yes\"/>\n" +
            "    <xsl:template match=\"/reportResults\">\n" +
            "        <div class=\"report\">\n" +
            "            <xsl:apply-templates select=\"reportResult\" />\n" +
            "        </div>\n" +
            "    </xsl:template>\n" +
            "\n" +
            "    <xsl:template match=\"reportResult\">\n" +
            "        <div class=\"result\">\n" +
            "            <xsl:apply-templates select=\"field\" />\n" +                    
            "        </div>\n" +
            "    </xsl:template>\n" +
            "\n" +
            "    <xsl:template match=\"field\">\n" +
            "        <div class=\"field\">\n" +
            "            <span>\n" +
            "                <xsl:attribute name=\"class\">\n" +
            "                    <xsl:value-of select=\"@type\" />\n" +
            "                </xsl:attribute>\n" +
            "                <xsl:value-of select=\"value\"/>\n" +                    
            "            </span>\n" +
            "        </div>\n" +
            "    </xsl:template>\n" +
            "</xsl:stylesheet>" + "\n\n";

    public void doTag() throws JspException, IOException {
        getHttpClient().executeMethod(getMethod());
        String response = getMethod().getResponseBodyAsString();
        //getJspContext().getOut().write(response);
        //System.out.println ("response: " + response);
        //Source xsltSource = new StreamSource(new StringReader(xslt));
        Source source = new StreamSource(new StringReader(response));
        ClassPathResource cpr = new ClassPathResource("gs/web/api/reports.xsl");

        JspWriter out = getJspContext().getOut();
        StringWriter wr = new StringWriter();

        try {
            gs.data.util.XMLUtil.transform(source, cpr.getInputStream(), wr);
            //gs.data.util.XMLUtil.transform(source, xsltSource, new StreamResult(wr));
            out.println(wr.getBuffer().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getReportUrl() {
        return _reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        _reportUrl = reportUrl;
    }

    public HttpClient getHttpClient() {
        if (_httpClient == null) {
            _httpClient = new HttpClient();
        }
        return _httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        _httpClient = httpClient;
    }

    // Getter and Setter on the _method field is required for
    // unit testing.
    GetMethod getMethod() {
        if (_method == null) {
            _method = new GetMethod(getReportUrl());
            _method.setRequestHeader("Accept", "application/xml");
        }
        return _method;
    }

    void setMethod(GetMethod method) {
        _method = method;
    }
}
