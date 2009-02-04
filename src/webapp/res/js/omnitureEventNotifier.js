// requires omniture script, trackit.js

var omnitureEventNotifier = {
    pageName: "",
    server: "",
    pageType: "''",
    heirarchy: "",
    channel: "",
    successEvents: "",
    sProps: {},
    eVars: {} ,

    send: function(){
        //alert("before send");
        this.fill();
        var s_code=s.t();
        if(s_code){
            document.write(s_code);
        }
        //alert("after send");
    },
    fill: function(){
         //alert("fill");
        s.pageName = this.pageName;
        s.server = this.server;
        s.channel = this.channel;
        s.pageType = this.pageType;
        s.hier1 = this.heirarchy;
        s.events = this.successEvents;

        for(var prop in this.sProps){
            s[prop] = this.sProps[prop];
        }

        for (var evar in this.eVars){
            s[evar] = this.eVars[evar];
        }
    },
    clear: function(){

        for(var propNum = 1; propNum <= 50; propNum++){
            var prop = "prop" + propNum;
            this.sProps[prop] = "";
        }

        for(var evarNum = 1; evarNum <= 50; evarNum++){
            var evar = "eVar" + evarNum;
            this.sProps[evar] = "";
        }
    }

};