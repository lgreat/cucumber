/**
 * Copied from global.js
 */
define(['cookies'],function(cookies) {

    /* circumventing browser restrictions on the number of cookies one can use */
    var subCookie = {
        nameValueSeparator: '$$:$$',
        subcookieSeparator: '$$/$$',

        propertyExpirySuffix : '.expiresInMilliSec',

        /*
         * Gets the entire object
         */
        getObject: function(cookieName){
            //alert("getObject()");
            var cookieValue = cookies.readEscapedCookie(cookieName);
            if (cookieValue == undefined || cookieValue.length < 1) {
                //alert("return cookie is null");
                return null;
            }

            var pairs =  cookieValue.split(subCookie.subcookieSeparator);
            //var result = new Array();  // fails with prototype lib loaded
            var result = {};

            //var debug = "";
            for (var i = 0; i < pairs.length; i++ )    {
                var nameValue = pairs[i].split(subCookie.nameValueSeparator) ;
                result[nameValue[0]] = nameValue[1];
                //debug += "\n" +  nameValue[0] + " = " +  nameValue[1];
            }
            //alert("cookie values" + debug);

            return result;
        },

        /*
         * Adds or Replaces the entire object
         */
        setObject: function(cookieName,subcookieObj,days){

            //alert( "setObject() " + cookieName + ", " + subcookieObj);
            var cookieValue = '';
            for (var property in subcookieObj)
            {
                if (cookieValue.length > 0){
                    cookieValue += subCookie.subcookieSeparator;
                }
                cookieValue += property + subCookie.nameValueSeparator;
                cookieValue += subcookieObj[property];
            }
            cookies.createCookie(cookieName,cookieValue,days);
        },

        /*
         * Add or replace a property of the cookie leaving the the other properties intact
         */
        setObjectProperty: function(cookieName, propertyName, propertyValue, days) {
            //alert( "setObjectProperty()");
            var cookieObj = subCookie.getObject(cookieName) ;
            if (cookieObj == undefined){
                cookieObj = {};
            }
            cookieObj[propertyName]  = propertyValue;
            subCookie.setObject(cookieName, cookieObj, days);
        },

        /*
         * Get a property of the cookie. Does not care if it has expired or not.
         */
        getObjectProperty: function(cookieName, propertyName) {
            var cookieObj = subCookie.getObject(cookieName) ;
            if (cookieObj === undefined || cookieObj === null ){
                return null;
            }
            var objPropertyValue = cookieObj[propertyName];
            if (objPropertyValue === undefined || objPropertyValue === null){
                return null;
            }else{
                return objPropertyValue;
            }
        },

        /*
         * Get a property of the cookie. Deletes the property if it has expired.
         */
        getObjectPropertyIfNotExpired: function(cookieName, propertyName) {
            var prop = subCookie.getObjectProperty(cookieName, propertyName);
            var returnProp = null;
            if (prop != null && prop != undefined && !(propertyName.indexOf(subCookie.propertyExpirySuffix) >= 0)) {

                var expiryInMills = subCookie.getObjectProperty(cookieName, propertyName + subCookie.propertyExpirySuffix);
                if (expiryInMills !== null && expiryInMills !== undefined) {
                    var expiryDate = new Date(parseInt(expiryInMills));
                    if (expiryDate !== 'Invalid Date') {
                        var today = new Date();
                        if (today > expiryDate) {
                            subCookie.deleteObjectProperty(cookieName, propertyName);
                            subCookie.deleteObjectProperty(cookieName, propertyName + subCookie.propertyExpirySuffix);

                            var cookieValue = cookies.readEscapedCookie(cookieName);
                            if (cookieValue === undefined || cookieValue.length < 1) {
                                //Delete the entire cookie if no more properties.
                                var someDateInPast = new Date();
                                someDateInPast.setDate(someDateInPast.getDate() - 10);
                                cookies.createCookieWithExpiresDate(cookieName, '', someDateInPast);
                            }

                        } else {
                            returnProp = prop;
                        }
                    }
                }
            }
            return returnProp;
        },

        /*
         * Deletes a property from a cookie object
         */
        deleteObjectProperty: function(cookieName, propertyName, days){
            var cookieObj = subCookie.getObject(cookieName) ;
            if (cookieObj == undefined){
                return;
            }
            delete cookieObj[propertyName];
            subCookie.setObject(cookieName,cookieObj,days);
        },


        /*
         * Creates a cookie with the property.Property names currently in use - showNLHoverOnArticles.
         */
        createAllHoverCookie : function (propertyName, propertyValue, expiryInDays) {
            subCookie.setObjectProperty("all_hover", propertyName, propertyValue, 365);
            var today = new Date();
            var expiryDate = new Date();
            expiryDate.setDate(today.getDate() + parseInt(expiryInDays));
            subCookie.setObjectProperty("all_hover", propertyName + subCookie.propertyExpirySuffix, expiryDate.getTime(), 365);
        }
    };

    return subCookie;

});