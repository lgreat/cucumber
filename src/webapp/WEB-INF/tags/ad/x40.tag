<jsp:directive.tag body-content="empty"/>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${!sessionScope.context.adFree}">
    <c:choose>
        <c:when test='${sessionScope.context.yahooCobrand}'>
            <iframe src="http://us.adserver.yahoo.com/a?f=96345362&p=ed&l=LREC&c=sh&bg=white" align="center" width="300" height="265" frameborder="no" border="0" marginwidth="0" marginheight="0" scrolling="no"></iframe>
        </c:when>
        <c:otherwise>
            <script type="text/javascript"><!--
                OAS_AD('x40');
            //--></script>
        </c:otherwise>
    </c:choose>
</c:if>

