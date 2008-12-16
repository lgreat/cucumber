// requires omniture script

var pageTracking = {
    pageName: "",
    server: "",
    hierarchy: "",

    send: function(){
        s.pageName = this.pageName;
        s.server = this.server;
        s.hier1 = this.hierarchy;
        var s_code=s.t();
        if(s_code){
            document.write(s_code);
        }
    }
};