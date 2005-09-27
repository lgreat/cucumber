<jsp:directive.tag body-content="empty"/>
<jsp:directive.attribute name="text" required="true"/>
<jsp:directive.attribute name="prevPage" required="true"/>
<jsp:directive.attribute name="nextPage" required="true"/>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<table id="footer" width="100%" border="0">
    <tr>
        <td width="100%" align="center">
            <a href="https://${sessionScope.context.secureHostName}/subscribe.page?state=${sessionScope.context.stateOrDefault.abbreviation}&host=${sessionScope.context.hostName}" target="_blank"><img src="/res/img/subscribe/conversion/popup/join_price_btn.gif" width="189" height="20" hspace="5" vspace="0" border="0"></a>
            <a href="http://${sessionScope.context.hostName}/cgi-bin/site/findschool2review.cgi/${sessionScope.context.stateOrDefault.abbreviation}/" target="_blank"><img src="/res/img/subscribe/conversion/try_sm_btn.gif" width="114" height="20" hspace="5" vspace="0" border="0"></a>
        </td>
        <td>
            <div class="quote" style="width:240px;">${text}</div>
            <div class="quotee">-Parent</div></td>
    </tr>
</table>
<table id="navlinks" width="100%" border="0" class="footernav">
    <tr>
        <td><a href="" onclick="window.close();">Close this window</a></td>
        <td align="right">
            <c:choose>
                <c:when test="${(prevPage != '') && (nextPage != '')}">
                    <a href="${prevPage}?state=${sessionScope.context.stateOrDefault.abbreviation}">&lt;&lt; Prev</a>
                    <img src="/res/img/subscribe/conversion/popup/p.gif" width="20" height="5" border="0" />
                    <a href="${nextPage}?state=${sessionScope.context.stateOrDefault.abbreviation}">Next &gt;&gt;</a>
                </c:when>
                <c:when test="${prevPage == ''}">
                    <a href="${nextPage}?state=${sessionScope.context.stateOrDefault.abbreviation}">Next &gt;&gt;</a>
                </c:when>
                <c:otherwise>
                    <a href="${prevPage}?state=${sessionScope.context.stateOrDefault.abbreviation}">&lt;&lt; Prev</a>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
</table>
