if (GSType == undefined) {
    var GSType = {};
}

GSType.yahooRegistrationHover = function() {
    this.dialog = null;
    this.width = 680;
    this.height = 600;
    this.baseUrl = '';
    this.domIdPrefix = '';
    this.init = function() {
        this.dialog = new YAHOO.widget.Dialog(this.domIdPrefix + "Dialog",
        {
            postmethod: "form",
            width: this.width + "px",
            visible: false,
            draggable: false,
            modal: true,
            close: false,
            zIndex: 9999999
        });
        this.dialog.render();
    };
    this.getIframeUrl = function() {
        return this.baseUrl;
    };
    this.show = function() {
        var frameLocation = '' + window.frames[this.domIdPrefix + 'IFrame'].window.location;
        if (frameLocation.indexOf(this.baseUrl) == -1) {
            window.frames[this.domIdPrefix + 'IFrame'].window.location = this.getIframeUrl();
        }

        document.getElementById(this.domIdPrefix + "Dialog").style.display="block";

        this.dialog.show();

        try {
            var dwidth = this.width;
            var dheight = this.height;
            var scrollTop = 0;
            if (document.all) {
                var iebody=(document.compatMode && document.compatMode != "BackCompat")?
                            document.documentElement : document.body;
                scrollTop = iebody.scrollTop;
            } else {
                scrollTop = window.pageYOffset;
            }
            var x = Math.round((YAHOO.util.Dom.getViewportWidth()-dwidth)/2);
            x = (x < 0)?0:x;
            var y = Math.round((YAHOO.util.Dom.getViewportHeight()-dheight)/2) + scrollTop;
            y = (y < scrollTop)?scrollTop:y;
            this.dialog.moveTo(x,y);
        } catch (e) {}

        return false;
    };

    this.close = function() {
        this.dialog.cancel();
        parent.sendToDestination();
    };
};
