    <jsp:directive.tag body-content="empty"/>
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
<jsp:directive.attribute
    name="extraJavascriptVariable"
    description="some syntactically correct javascript to followt he fc_content declaration. For example: ''var fc_track='paid';''"
    required="false"/>

<script language="javascript" type="text/javascript">
    var fc_content='${sessionScope.context.hostName}/${empty(state) ? sessionScope.context.stateOrDefault.abbreviation : state}/${empty(pageType) ?
    "-" : pageType}/${pageName}/${empty(id) ?
    "-" : id}/${empty(schoolType) ?
    "-" : schoolType}/${empty(schoolLevel) ?
    "-" : schoolLevel}/${empty(locale) ?
    "-" : locale}';
    ${empty(extraJavascriptVariable) ? "" : extraJavascriptVariable}
    // Fireclick Web Analytics - COPYRIGHT 1999-2005 - Please do not modify this code
    function handle(){return true;}
    window.onerror=handle;
    var fc_host='www.greatschools.net';
    document.write('
    <scr'+' ipt '
        +' src="'+((location.protocol=='http:')?'http:':'https:')
       +'//a248.e.akamai.net/f/248/5462/3h/hints.netflame.cc/service/sc'+'ript/'+fc_host+'"></scr'+'ipt>');
    function fcce(){if (typeof(fcnf)!="undefined") fcnf();}
    var fcfn=window.onload;
    function fcco(){window.setTimeout("fcce();", 100);fcfn();}
    window.onload= null==fcfn ? fcce:fcco;
</script>
