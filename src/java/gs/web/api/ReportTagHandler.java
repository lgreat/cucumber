package gs.web.api;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Source;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import gs.web.util.UrlUtil;

/**
 * @author chriskimm@greatschools.net
 */
public class ReportTagHandler extends SimpleTagSupport {

    private String _type;
    private HttpClient _httpClient;
    private GetMethod _method;
    private String _display = "div"; // default
    private String _key = "1234abc"; // default

    public void doTag() throws JspException, IOException {
        getHttpClient().executeMethod(getMethod());
        String response = getMethod().getResponseBodyAsString();
        //getJspContext().getOut().write(response);
        //System.out.println ("response: " + response);
        //Source xsltSource = new StreamSource(new StringReader(xslt));
        Source source = new StreamSource(new StringReader(response));
        Source xsltSource = getXsltSource();

//        ClassPathResource cpr = new ClassPathResource("gs/web/api/reports.xsl");

        JspWriter out = getJspContext().getOut();
        StringWriter wr = new StringWriter();

        try {
//            gs.data.util.XMLUtil.transform(source, cpr.getInputStream(), wr);
            gs.data.util.XMLUtil.transform(source, xsltSource, new StreamResult(wr));
            out.println(wr.getBuffer().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
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

    String getReportUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        if (getJspContext() instanceof PageContext) {
            PageContext pc = (PageContext)getJspContext();
            String host = pc.getRequest().getLocalName();
            if (host.contains("dev.")) {
                sb.append("api.dev.greatschols.net");
            } else if (host.contains("staging.")) {
                sb.append("api.staging.greatschols.net");
            } else if (UrlUtil.isDeveloperWorkstation(host)) {
                sb.append(host);
                String port = String.valueOf(pc.getRequest().getLocalPort());
                if (StringUtils.isNotBlank(port) && !"80".equals(port)) {
                    sb.append(":").append(port);
                }
                sb.append("/apiservice");
            }
        }
        sb.append("/reports/").append(getType());
        return sb.toString();
    }

    // Getter and Setter on the _method field is required for unit testing.
    GetMethod getMethod() {
        if (_method == null) {
            System.out.println ("url: " + getReportUrl());
            _method = new GetMethod(getReportUrl());
            _method.setRequestHeader("Accept", "application/xml");
            _method.setQueryString("key=" + getKey());
            try {
                System.out.println ("uri: " + _method.getURI().toString());
            } catch (Exception e) { e.printStackTrace(); }
        }
        return _method;
    }

    void setMethod(GetMethod method) {
        _method = method;
    }

    public String getKey() {
        return _key;
    }

    public void setKey(String key) {
        _key = key;
    }

    public String getDisplay() {
        return _display;
    }

    public void setDisplay(String display) {
        _display = display;
    }

    public Source getXsltSource() throws IOException {
        String style = getDisplay();
        String xsl = "gs/web/api/reports.xsl"; // default
        if ("table".equals(style)) {
            xsl = "gs/web/api/reports-table.xsl";
        }
        ClassPathResource cpr = new ClassPathResource(xsl);
        return new StreamSource(cpr.getInputStream());
    }
}
