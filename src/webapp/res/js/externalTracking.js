// requires omniture script

var pageTracking = {
    pageName: "",
    server: "",
    hierarchy: "",
    successEvents: "",
    eVars: {},

    send: function(){
        s.pageName = this.pageName;
        s.server = this.server;
        s.hier1 = this.hierarchy;
        s.events = this.successEvents;
        for (var evar in this.eVars){
            s[evar] = this.eVars[evar];
        }        
        var s_code=s.t();
        if(s_code){
            document.write(s_code);
        }
    },
    clear: function() {
        this.pageName = "";
        this.hierarchy = "";
        this.successEvents = "";
        this.eVars = {};
    }
};