    <jsp:directive.tag body-content="empty"/>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:directive.attribute name="pageType" required="true"/>
<jsp:directive.attribute name="pageName" required="true"/>
<jsp:directive.attribute
    name="id"
    description="school, district or article"
    required="false"/>
<jsp:directive.attribute
    name="state"
    description="override of the state stored in the session"
    required="false"/>
<jsp:directive.attribute
    name="schoolType"
    description="public, private or charter"
    required="false"/>
<jsp:directive.attribute
    name="schoolLevel"
    description="e,m,h or combinations thereof"
    required="false"/>
<jsp:directive.attribute
    name="locale"
    description="could be county, city or metro somethingerother"
    required="false"/>

    <!-- SiteCatalyst code version: H.2. Copyright 1997-2005 Omniture, Inc. More info available at http://www.omniture.com -->
    <gsml:url var="js" value="/res/js/s_code.js" />
    <script language="JavaScript" src="${js}"></script>
    <script language="JavaScript"><!--
    s.pageName="${pageName}"
    s.server="${requestScope.context.hostName}"
    s.channel="US:${empty(state) ? requestScope.context.stateOrDefault.abbreviationLowerCase : state}"
    s.pageType="${empty(pageType) ? "" : pageType}"
    s.prop1="${empty(id) ? "" : id}"
    s.prop2="${empty(schoolType) ? "" : schoolType}"
    s.prop3="${empty(schoolLevel) ? "" : schoolLevel}"
    s.prop4="${empty(locale) ? "" : locale}"
    s.prop5="${requestScope.context.stateOrDefault.subscriptionState ? "1" : "0"}"
    /* E-commerce Variables */
    s.campaign=""
    s.state=""
    s.zip=""
    s.events=""
    s.products=""
    s.purchaseID=""
    s.eVar1=""
    s.eVar2=""
    s.eVar3=""
    s.eVar4=""
    s.eVar5=""
    /************* DO NOT ALTER ANYTHING BELOW THIS LINE ! **************/
    var s_code=s.t();if(s_code)document.write(s_code)//--></script>
    <script language="JavaScript"><!--
    if(navigator.appVersion.indexOf('MSIE')>=0)document.write(unescape('%3C')+'\!-'+'-')
    //--></script><noscript><img
    src="http://gsnetdev.122.2O7.net/b/ss/gsnetdev/1/H.2--NS/0"
    height="1" width="1" border="0" alt="" /></noscript><!--/DO NOT REMOVE/-->
    <!-- End SiteCatalyst code version: H.2. -->


