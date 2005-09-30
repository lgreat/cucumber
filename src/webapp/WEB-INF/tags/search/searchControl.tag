<%@ tag body-content="empty" %>
<%@ taglib prefix="gsml" tagdir="/WEB-INF/tags/gsml" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<form id="searchForm" name="sf" method="get" style="margin: 0; padding: 0"
      action="/search.page">
    <table style="width:100%">
        <tr>
            <td class="label" width="90">Keywords:</td>
            <td>
                <input type="text"
                       id="q"
                       name="q"
                       style="width:95%"
                       size="50"
                       maxlength="255"
                       value="${not empty param.q ? param.q : ''}"
                       onfocus="this.className='focus'"
                       onblur="this.className=''"/>
            </td>

            <td>
                <input type="image" src="res/img/search/new_go_blue.gif"
                       value=" Go "/>
            </td>
        </tr>
        <tr>
            <c:choose>
               <c:when test="${empty param.state || param.state == 'all'}">
                  <td class="label">
                     Location:
                  </td>
                  <td>
                     <gsml:stateSelector/>
                      <a href="http://www.greatschools.net/cgi-bin/template_plain/advanced/">Search near address</a>
                  </td>

               </c:when>
               <c:otherwise>
                  <td><input type="hidden" name="state" value="${param.state}"/></td>
                  <td>
                    <a href="http://www.greatschools.net/cgi-bin/template_plain/advanced/${param.state}">Search near address</a>
                  </td>
                   <tr>
                       <td class="label">Browse:</td>
                       <td>
                           <a href="http://www.greatschools.net/content/allArticles.page?state=${param.state}"
                              class="pad">All Articles</a>
                           <a href="http://www.greatschools.net/modperl/citylist/${param.state}/"
                              class="pad">All Cities</a>
                           <a href="http://www.greatschools.net/modperl/distlist/${param.state}"
                              class="pad">All Districts</a>
                       </td>
                   </tr>


               </c:otherwise>
            </c:choose>
        </tr>

    </table>
</form>
