<%@ tag import="gs.data.content.IArticleDao"%>
<jsp:directive.tag body-content="empty"/>
<jsp:directive.attribute name="articleId" required="true"/>
<jsp:scriptlet>
    gs.web.ISessionFacade context = gs.web.SessionFacade.getInstance(request);
    gs.data.state.State state = context.getState();
    IArticleDao dao = (IArticleDao) context.getApplicationContext().getBean(IArticleDao.BEAN_ID);
    boolean isAvailable = dao.isArticleInState(articleId, state);
    if (isAvailable) {
        String title = dao.getTitleFromId(state, new Integer(articleId));
</jsp:scriptlet>
<li>
    <a href="http://${context.hostName}/cgi-bin/showarticle/${context.stateOrDefault.abbreviation}/${articleId}/">
        <jsp:scriptlet>
        out.print(title);
        </jsp:scriptlet>
    </a>
</li>
<jsp:scriptlet>
    }
</jsp:scriptlet>
