// requires omniture script, trackit.js

var pageTracking = {
    pageName: "",
    server: "",
    pageType: "''",
    heirarchy: "",
    channel: "",
    events: "",
    sProp: {},
    eVar: {} ,

    send: function(){
        //alert("before send");
        this.fill();
        var s_code=s.t();
        if(s_code){
            document.write(s_code);
        }
        alert("after send");
    },
    fill: function(){
         //alert("fill");
         s.pageName = this.pageName;
         s.server = this.server;
         s.channel = this.channel;
         s.pageType = this.pageType;
         s.hier1 = this.heirarchy;
         s.events = clickCapture.getEvents(this.events);


         //not collecting sprops nor eVars on this page at this time
         //but will grab the values currently sitting in the cookie.
         s.prop1 = clickCapture.getProp(1) ;
         s.prop2 = clickCapture.getProp(2);
         s.prop3 = clickCapture.getProp(3);
         s.prop4 = clickCapture.getProp(4);
         s.prop5 = clickCapture.getProp(5);
         s.prop6 = clickCapture.getProp(6);
         s.prop7 = clickCapture.getProp(7);
         
         s.prop8 = clickCapture.getProp(8,s.getQueryParam('cpn'));
         if (s.prop8 == '' ){
             s.prop8 = s.pageName ;
         }

         var locationAnchor = '';
         if (location.href && location.href.indexOf('#from..') > -1) {
             locationAnchor = location.href.substring(location.href.indexOf('#from..')+7);
             s.prop9 = s.pageName + ' ' + locationAnchor;
         } else {
             s.prop9 = s.pageName + ' ' + clickCapture.getProp(9);
         }

         s.prop10 = clickCapture.getProp(10);
         s.prop11 = clickCapture.getProp(11);

         //s.prop13 is being set on the article.page to distinguish new and old articles
         s.prop13 =  clickCapture.getProp(13);


         s.eVar1 = clickCapture.getEVar(1);
         s.eVar2 = clickCapture.getEVar(2);
         s.eVar3 = clickCapture.getEVar(3);
         s.eVar4 = clickCapture.getEVar(4);
         s.eVar5 = clickCapture.getEVar(5);
         s.eVar6 = clickCapture.getEVar(6);
         s.eVar7 = clickCapture.getEVar(7);
         s.eVar8 = clickCapture.getEVar(8);
         s.eVar9 = clickCapture.getEVar(9);
         s.eVar10 = clickCapture.getEVar(10);
    }

}





