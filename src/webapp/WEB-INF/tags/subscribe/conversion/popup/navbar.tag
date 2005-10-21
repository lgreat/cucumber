<jsp:directive.tag body-content="empty"/>
<jsp:directive.attribute name="current" required="true"/>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<table id="navbar" cellpadding="0" cellspacing="0" border="0" width="100%">
  <tr>
    <td>
        <c:choose>
            <c:when test="${(current == 'schoolChoiceOptions')}">
                <div class="menuBaractive">
            </c:when>
            <c:otherwise>
                <div class="menuBar">
            </c:otherwise>
        </c:choose>
        <a href="/subscribe/conversion/popup/schoolChoiceOptions.page?state=${requestScope.context.stateOrDefault.abbreviation}">School Choice<br/>Options</a></div></td>
    <td>
         <c:choose>
             <c:when test="${(current == 'topRatedSchools')}">
                 <div class="menuBaractive">
             </c:when>
             <c:otherwise>
                 <div class="menuBar">
             </c:otherwise>
         </c:choose>
        <a href="/subscribe/conversion/popup/topRatedSchools.page?state=${requestScope.context.stateOrDefault.abbreviation}">Top-Rated<br/>Schools</a></div></td>
    <td>
        <c:choose>
            <c:when test="${(current == 'ratings')}">
                <div class="menuBaractive">
            </c:when>
            <c:otherwise>
                <div class="menuBar">
            </c:otherwise>
        </c:choose>
        <a href="/subscribe/conversion/popup/ratings.page?state=${requestScope.context.stateOrDefault.abbreviation}">GreatSchools<br/>Ratings</a></div></td>
    <td>
        <c:choose>
            <c:when test="${(current == 'parentReviews')}">
                <div class="menuBaractive">
            </c:when>
            <c:otherwise>
                <div class="menuBar">
            </c:otherwise>
        </c:choose>
        <a href="/subscribe/conversion/popup/parentReviews.page?state=${requestScope.context.stateOrDefault.abbreviation}">Parent<br/>Reviews</a></div></td>
    <td>
        <c:choose>
            <c:when test="${(current == 'flagAlerts')}">
                <div class="menuBaractive">
            </c:when>
            <c:otherwise>
                <div class="menuBar">
            </c:otherwise>
        </c:choose>
        <a href="/subscribe/conversion/popup/flagAlerts.page?state=${requestScope.context.stateOrDefault.abbreviation}">Flag<br/>Alerts</a></div></td>
    <td>
        <c:choose>
            <c:when test="${(current == 'compareSchools')}">
                <div class="menuBaractive">
            </c:when>
            <c:otherwise>
                <div class="menuBar">
            </c:otherwise>
        </c:choose>
        <a href="/subscribe/conversion/popup/compareSchools.page?state=${requestScope.context.stateOrDefault.abbreviation}">Compare<br/>Schools</a></div></td>
  </tr>
</table>