// GS-8291
var mssAutoHoverInterceptor = {
    interceptPercent: 0,
    hoverName: '',
    hoverUrl: '',
    hoverHeight: 0,
    hoverWidth: 0,
    newHoverName: '',
    cookieName: '',
    cookieProperty: '',
    userLoggedInCookieName: '',
    ajaxRequest: null,
    hasSeenMssHover: false,
    shouldIntercept: function(hoverName) {
        if (this.hoverName != hoverName){
            return false;
        }
        if (this.hasSeenMssHover) {
            return false;
        }
        if (this.userIsLoggedIn(this.userLoggedInCookieName)){
            return false;
        }

        var dataObject = subCookie.getObject(this.cookieName);
        // if no cookie, set value to 1 and show hover
        if (dataObject == undefined || dataObject[this.cookieProperty] == undefined){
            subCookie.setObjectProperty(this.cookieName,this.cookieProperty,1,1);
            return true;
        }
        // else no-op
        return false;
    },
    onlyCheckIfShouldIntercept : function(hoverName) {
        if (this.hoverName != hoverName){
            return false;
        }
        if (this.hasSeenMssHover) {
            return false;
        }
        if (this.userIsLoggedIn(this.userLoggedInCookieName)){
            return false;
        }

        var dataObject = subCookie.getObject(this.cookieName);
        // if no cookie, set value to 1 and show hover
        if (dataObject == undefined || dataObject[this.cookieProperty] == undefined){
            return true;
        }
        // else no-op
        return false;
    },
    setHasSeenMssHover: function(bool) {
        this.hasSeenMssHover = bool;
    },
    init: function(){
        this.hoverName = 'mssAutoHover';
        this.cookieName = 'mssAutoHover';
        this.cookieProperty = 'GS-8290';
        this.userIsLoggedIn = GS_userIsLoggedIn;
        this.userLoggedInCookieName = 'MEMID';
    },
    userShouldSeeAlternateHover: function(){return false;},
    generateRandomNumber: function(){return 0;},
    userIsLoggedIn: function(){return false;}
};

mssAutoHoverInterceptor.init();
