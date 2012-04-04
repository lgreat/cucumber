define(['require','exports','./clickCapture', 'properties', 'sCode','global'], function(require, exports, clickCapture, properties, sCode) {

    // put clickCapture into global scope to avoid rewriting GSWeb for now:
    GS.log('copying clickCapture module to global scope');
    window.clickCapture = clickCapture;

    return {
        pageName: "",
        server: "",
        hierarchy: "",
        successEvents: "",
        // I tried to wrap s_code in a module, but it would require too many mods to s_code_dev
        s: window.s,
        clickCapture: clickCapture,
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
                var span = document.createElement('span');
                var body = document.getElementsByTagName('body')[0];
                span.innerHTML = s_code;
                body.appendChild(span);
            }
        },
        clear: function() {
            this.pageName = "";
            this.hierarchy = "";
            this.successEvents = "";
            this.eVars = {};
        }
    };
});
