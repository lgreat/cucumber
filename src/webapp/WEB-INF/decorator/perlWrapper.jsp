<%@ page import="gs.web.SessionContext"%>
<%@ page import="gs.data.state.State"%>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<decorator:usePage id="smPage"/>
<%
    // Determine if we're on a server or a developers workstation with no perl
    String serverName = request.getServerName();
    boolean developerWorkstation = false;
    if (serverName.indexOf("localhost") > -1) {
        developerWorkstation = true;
        serverName = "dev.greatschools.net";
        String baseUrlDev = request.getScheme() + "://" + serverName + "/";
        String baseUrlJava = request.getRequestURL().toString();
        pageContext.setAttribute("baseUrlDev", baseUrlDev, PageContext.REQUEST_SCOPE);
        pageContext.setAttribute("baseUrlJava", baseUrlJava, PageContext.REQUEST_SCOPE);
    }
    pageContext.setAttribute("developerWorkstation",
            (developerWorkstation) ? Boolean.TRUE : Boolean.FALSE, PageContext.REQUEST_SCOPE);

    // The page for the c:import to use
    String wrapperStyle = smPage.getProperty("meta.wrapperstyle");
    SessionContext ctx = SessionContext.getInstance(request);
    State state = ctx.getStateOrDefault();
    String javaWrapperUrl = "http://" + serverName + "/modperl/javawrapper/" +
        state.getAbbreviation() + "/" + wrapperStyle;
    pageContext.setAttribute("javaWrapperUrl", javaWrapperUrl, PageContext.REQUEST_SCOPE);
%>
<c:import url="${javaWrapperUrl}" var="ctmpl"/>
<%
    String ctmpl = (String) pageContext.getAttribute("ctmpl");

    // Get the position markers
    int startmarker = ctmpl.indexOf("<html>");
    int bodymarker = ctmpl.lastIndexOf("<span id=\"replace\">REPLACE</span>");
    int titlestartmarker = ctmpl.indexOf("<title>");
    int titlestopmarker = ctmpl.indexOf("</title>");
    int headstopmarker = ctmpl.indexOf("</head>");

    // Write out the start through title
    out.write(ctmpl.substring(startmarker, titlestartmarker));
%>
<title><decorator:title default="REPLACE THIS REPLACE THIS REPLACE THIS REPLACE THIS"/></title>
<c:if test="${developerWorkstation}"><base href="${baseUrlDev}"/></c:if>
<%
    // Write out the head element
    out.write(ctmpl.substring(titlestopmarker + 8, headstopmarker));
%>
<c:if test="${developerWorkstation}"><base href="${baseUrlJava}"/></c:if>
<decorator:head/>
<c:if test="${developerWorkstation}"><base href="${baseUrlDev}"/></c:if>
<%
    // Write out the head through the body start
    out.write(ctmpl.substring(headstopmarker, bodymarker));
%>
<c:if test="${developerWorkstation}"><base href="${baseUrlJava}"/></c:if>
<decorator:body/>
<c:if test="${developerWorkstation}"><base href="${baseUrlDev}"/></c:if>
<%
    out.write(ctmpl.substring(bodymarker + 33, ctmpl.length() - 1));
%>