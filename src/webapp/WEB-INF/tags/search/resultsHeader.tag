<jsp:directive.tag body-content="empty"/>
<jsp:directive.attribute name="total" required="true"/>
<jsp:directive.attribute name="page" required="true"/>
<jsp:directive.attribute name="query" required="true"/>
<jsp:directive.attribute name="constraint" required="true"/>
<jsp:directive.attribute name="style" required="true"/>
<jsp:directive.attribute name="pageSize" required="true"/>
<jsp:directive.attribute name="type" required="true"/>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


    <c:choose>
        <c:when test="${total > 0}">
Results
            <c:out value="${((empty page || page le 1) ? 0 : (page-1) * pageSize)+1}"/>
-
            <c:choose>
                <c:when test="${(((empty page || page le 1) ? 0 : (page-1) * pageSize) + pageSize) le total}">
                    <c:out value="${((empty page || page le 1) ? 0 : (page-1) * pageSize) + pageSize}"/>
                </c:when>
                <c:otherwise>
                    <c:out value="${total}"/>
                </c:otherwise>
            </c:choose>
of
            <c:out value="${total}"/>

            <c:if test="${(total + 0) gt (pageSize + 0)}">
                <c:choose>

                    <c:when test="${param.c == type}">

                        <span class="pageLinks">
                                        Page:
                            <c:set var="maxPages"/>

                            <c:if test="${page gt 1}">
                                <a class="p"
                                    href="/search.page?q=${query}&amp;c=${constraint}&amp;s=${style}&amp;p=${page-1}">&lt;&lt;</a>
                            </c:if>
                            <c:set var="counter" value="1"/>
                            <c:forEach begin="${page le 10 ? 1 : page-5}"
                                end="${(total / pageSize) + ((total % pageSize) > 0 ? 1 : 0)}"
                                var="index">

                                <c:if test="${counter lt 11}">
                                    <c:if test="${page == index}">
                                        <c:set var="tick" value="currentPage"/>
                                    </c:if>
                                    <span id="${tick}">
                                        <a class="p"
                                            href="/search.page?q=${query}&amp;c=${constraint}&amp;s=${style}&amp;p=${index}"> ${index}</a>
                                    </span>
                                    <c:set var="tick" value=""/>
                                    <c:set var="counter" value="${counter+1}"/>
                                </c:if>
                                <c:set var="maxPages" value="${index}"/>
                            </c:forEach>
                            <c:if test="${page lt maxPages}">
                                <a class="p"
                                    href="/search.page?q=${query}&amp;c=${constraint}&amp;s=${style}&amp;p=${page+1}">&gt;&gt;</a>
                            </c:if>
                        </span>
                    </c:when>
                    <c:otherwise>
                        <span class="more">
                            <a href="/search.page?q=${query}&amp;c=${type}&amp;s=${style}&amp;p=1">more >></a>
                        </span>
                    </c:otherwise>
                </c:choose>
            </c:if>
        </c:when>
        <c:otherwise>
            <c:out value="No ${type}s found for query: "/>
            <span style="font-weight:bold; font-style:italic">
                <c:out value="${query}"/>
            </span>
        </c:otherwise>
    </c:choose>