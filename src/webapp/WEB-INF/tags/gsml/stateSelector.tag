<%@ tag body-content="empty" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:useBean id="states" class="gs.data.state.StateManager"/>
<select name="state">
    <option name="state" value="all">State</option>
    <c:forEach items="${states.iterator}" var="state">
        <option name="state" ${param.state == state ? 'selected' : ''}
                value="${state}">
            <c:out value="${state.abbreviation}"/>
        </option>
    </c:forEach>
</select>