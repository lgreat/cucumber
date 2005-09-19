<jsp:directive.tag body-content="empty"/>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${!sessionScope.context.adFree}">
        <script type="text/javascript"><!--
            OAS_AD('x40');
        //--></script>
</c:if>
