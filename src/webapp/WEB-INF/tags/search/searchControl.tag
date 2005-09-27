<%@ tag body-content="empty" %>
<%@ taglib prefix="gsml" tagdir="/WEB-INF/tags/gsml" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
    gs.web.SessionContext context = gs.web.SessionContext.getInstance(session);
    gs.data.state.State state = context.getState();
%>

<form id="searchForm" name="sf" method="get" style="margin: 0; padding: 0" action="/search.page">
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
                <input type="image" src="res/img/search/new_go_blue.gif" value=" Go "/>
             </td>
        </tr>
        <tr>
      <% if (state == null) { %>
        <td class="label">
            <!--<span style="margin-left:3mm; margin-right:2mm">in:</span>-->
            <gsml:stateSelector/>
        </td>
      <% } %>
        <td>
            <a href="">Search near address</a>
        </td>
        </tr>
        <% if (state != null) { %>
            <tr>
                <td><span class="message">Browse</span></td>
                <td>
                    <a href="" class="pad">All Articles</a>
                    <a href="" class="pad">All Cities</a>
                    <a href="" class="pad">All Districts</a>
                </td>
            </tr>
        <% } %>
    </table>
</form>
