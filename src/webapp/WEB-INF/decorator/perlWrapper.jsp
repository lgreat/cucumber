<%@ page import="gs.web.ISessionFacade,
                 gs.web.SessionFacade,
                 gs.data.util.NetworkUtil"%>
<%@ page import="gs.data.state.State"%>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<decorator:usePage id="smPage"/>
<%
    ISessionFacade ctx = SessionFacade.getInstance(request);

    NetworkUtil networkUtil = new NetworkUtil();

    // Determine if we're on a server or a developers workstation with no perl
    String serverName = request.getServerName();
    boolean developerWorkstation = networkUtil.isDeveloperWorkstation(serverName);
    pageContext.setAttribute("developerWorkstation", Boolean.valueOf(developerWorkstation), PageContext.REQUEST_SCOPE);

    if (developerWorkstation) {
        serverName = "dev.greatschools.net";
        String baseUrlDev = request.getScheme() + "://" + serverName + "/";
        String baseUrlJava = request.getRequestURL().toString();
        pageContext.setAttribute("baseUrlDev", baseUrlDev, PageContext.REQUEST_SCOPE);
        pageContext.setAttribute("baseUrlJava", baseUrlJava, PageContext.REQUEST_SCOPE);
    }

    // The page for the c:import to use
    String wrapperStyle = smPage.getProperty("meta.wrapperstyle");
    State state = ctx.getStateOrDefault();
    String memberParam = (ctx.getUser() != null)?"?member=" + ctx.getUser().getId():"";
    String javaWrapperUrl = "http://" + serverName + "/modperl/javawrapper/" +
        state.getAbbreviation() + "/" + wrapperStyle + memberParam;
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