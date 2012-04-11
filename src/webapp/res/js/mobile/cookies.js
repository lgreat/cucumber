/**
 * Copied from global.js
 */
define(function() {
    //read cookie, return "" if cookie not found or cookie not set
    var readCookie = function(cookieName) {
        var cookie = "" + document.cookie;
        var i = cookie.indexOf(cookieName);
        if (i == -1 || cookieName == "") return "";

        var j = cookie.indexOf(';', i);
        if (j == -1) j = cookie.length;

        return unescape(cookie.substring(i + cookieName.length + 1, j));
    };

    var hasCookie = function(cookieName) {
        return (document.cookie.length > 0 && document.cookie.indexOf(cookieName+"=") >= 0);
    };

    var getCookieExpiresDate = function(days,hours,minutes,seconds) {
        var date = new Date();
        var offset = 0;
        if (days) {
            offset += (days*24*60*60*1000);
        }
        if (hours) {
            offset += (hours*60*60*1000);
        }
        if (minutes) {
            offset += (minutes*60*1000);
        }
        if (seconds) {
            offset += (seconds*1000);
        }
        date.setTime(date.getTime() + offset);
        return date;
    };

    // create a cookie with name=value and optional "days" expiration
    var createCookieWithExpiresDate = function(name, value, date) {
        var expires = "";
        if (date && date.toGMTString) {
            expires = "; expires=" + date.toGMTString();
        }

        /* set the domain to empty string if running on a dev machine */
        if (location.hostname == "localhost" || location.hostname == "") {
            var domain = "";
        }else{
            var domain = "; domain=greatschools.org";
        }
        document.cookie = name + "=" + escape(value) + expires + "; path=/" + domain;
    };

    // create a cookie with name=value and optional "days" expiration
    var createCookie = function(name, value, days) {
        if (days) {
            createCookieWithExpiresDate(name, value, getCookieExpiresDate(days));
        } else {
            createCookieWithExpiresDate(name, value);
        }
    };

    //read cookie, return "" if cookie not found or cookie not set
    var readEscapedCookie = function(cookieName) {
        var cookie = "" + document.cookie;
        var i = cookie.indexOf(cookieName);
        if (i == -1 || cookieName == "") return "";

        var j = cookie.indexOf(';', i);
        if (j == -1) j = cookie.length;
        var value = cookie.substring(i + cookieName.length + 1, j) ;

        return unescape(value);
    };


    return {
        createCookie:createCookie,
        createCookieWithExpiresDate:createCookieWithExpiresDate,
        readCookie:readCookie,
        readEscapedCookie:readEscapedCookie,
        hasCookie:hasCookie,
        getCookieExpiresDate:getCookieExpiresDate
    }


});