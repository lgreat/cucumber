if (GSType == undefined) {
    var GSType = {};
}

var destUrl = '';
function sendToDestination() {
    if (window.nthGraderRegistrationDialog != undefined) {
        window.nthGraderRegistrationDialog.dialog.cancel();
    }
    if (window.autoMssHover != undefined) {
        window.autoMssHover.dialog.cancel();
    }
    if (window.ldRegistrationDialog != undefined) {
        window.ldRegistrationDialog.dialog.cancel();
    }

    if (destUrl != '' && destUrl != 'reload') {
        window.location = destUrl;
        destUrl = '';
    } else if (destUrl == 'reload') {
        destUrl = '';
        window.location.href=window.location.href;
        window.location.reload();
    }
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
    this.isCookieSet = function(cookieName){
        // if cookie cookieName exists, the cookie is set
        var value = readCookie(cookieName);

        return value != undefined && value.length > 0;
    };
    this.getServerName = function() {
        // default to live
        var serverName = 'www';

        // check for staging
        if (location.hostname.match(/staging\.|staging$/)) {
            serverName = 'staging';
        } else if (location.hostname.match(/dev\.|dev$|clone|\.office\.|cpickslay\.|localhost|127\.0\.0\.1|macbook/)) {
            serverName = 'dev';
        }

        return serverName;
    };
    // override to do your own preparation
    this.prepAndShow = function(destination) {
        destUrl = destination;
        return this.checkCookies(destination);
    };
    this.checkCookies = function(destination) {
        // check for cookies
        // if no cookies
        if (!this.isCookieSet('MEMID')) {
            // proceed as usual, show hover
            return this.show();
        } else {
            // if just MEMID
            if (!this.isCookieSet('community_' + this.getServerName())) {
                // send to loginOrRegister.page with redirect set to destination
                var loginUrl = '/community/loginOrRegister.page?redirect=';
                loginUrl += encodeURIComponent(destination);
                window.location = loginUrl;
                return false;
            } else {
                // both memid and community
                // send directly to destination
                return true;
            }
        }
    };
    this.show = function() {
        window.frames[this.domIdPrefix + 'IFrame'].window.location = this.getIframeUrl();

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
        sendToDestination();
    };
};
