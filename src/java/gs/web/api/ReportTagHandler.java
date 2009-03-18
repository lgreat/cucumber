package gs.web.api;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.jdom.input.SAXBuilder;
import org.jdom.JDOMException;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Source;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import gs.web.util.UrlUtil;
import gs.web.jsp.SpringTagHandler;
import gs.data.api.IApiAccountDao;
import gs.data.api.ApiAccount;

/**
 * @author chriskimm@greatschools.net
 */
public class ReportTagHandler extends SpringTagHandler {

    private String _type;
    private HttpClient _httpClient;
    private GetMethod _method;
    private String _display = "div"; // default
    private String _key = "1234abc"; // default
    private IApiAccountDao _accountDao;

    public void doTag() throws JspException, IOException {
        getHttpClient().executeMethod(getMethod());
        String xml = getMethod().getResponseBodyAsString();
        String decoratedXml = decorateWithAccountInfo(xml);
        Source source = new StreamSource(new StringReader(decoratedXml));
        Source xsltSource = getXsltSource();
        JspWriter out = getJspContext().getOut();
        StringWriter wr = new StringWriter();

        try {
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

        PageContext pc;
        if (getJspContext() instanceof PageContext) {
            pc = (PageContext)getJspContext();
        } else {
            pc = (PageContext) getJspContext().findAttribute(PageContext.PAGECONTEXT);
        }

        String host = pc.getRequest().getServerName();
        if (host.contains("dev.")) {
            sb.append("api.dev.greatschools.net");
        } else if (host.contains("staging.")) {
            sb.append("api.staging.greatschools.net");
        } else if (UrlUtil.isDeveloperWorkstation(host)) {
            sb.append(host);
            String port = String.valueOf(pc.getRequest().getLocalPort());
            if (StringUtils.isNotBlank(port) && !"80".equals(port)) {
                sb.append(":").append(port);
            }
            sb.append("/apiservice");
        } else {
            sb.append("api.greatschools.net");
        }

        sb.append("/reports/").append(getType());
        return sb.toString();
    }

    // Getter and Setter on the _method field is required for unit testing.
    GetMethod getMethod() {
        if (_method == null) {
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

    public String decorateWithAccountInfo(String response) {
        String decoratedXml = response;
        try {
            SAXBuilder builder = new SAXBuilder();
            org.jdom.Document doc = builder.build(new StringReader(response));
            Element root = doc.getRootElement();
            List<Element> results = root.getChildren("result");
            for (Element result : results) {
                List<Element> fields = result.getChildren("field");
                for (Element field : fields) {
                    if ("api_key".equals(field.getAttributeValue("type"))) {
                        Element value = field.getChild("value");
                        ApiAccount account = getAccountDao().getAccountByKey(value.getTextTrim());
                        field.setAttribute("type", "account");
                        if (account != null) {
                            value.setText(account.getName());
                        } else {
                            value.setText("null");
                        }
                    }
                }
            }
            XMLOutputter out = new XMLOutputter();
            decoratedXml = out.outputString(doc);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (JDOMException je) {
            je.printStackTrace();
        }
        return decoratedXml;
    }

    public IApiAccountDao getAccountDao() {
        if (_accountDao == null) {
            _accountDao = (IApiAccountDao)getApplicationContext().getBean(IApiAccountDao.BEAN_ID);
        }
        return _accountDao;
    }

    public void setAccountDao(IApiAccountDao accountDao) {
        _accountDao = accountDao;
    }
}
