var GS = GS || {};
GS.uri = GS.uri || {};

GS.uri.Uri = function() {
    //TODO: create a stateful Uri object that contains a querystring params hash... something better
    //than what was copied in below.

};


/**
 * Written for GS-12127. When necessary, make ajax calls prepend result of this method to relative path, in order
 * to override any <base> tag that's on the page, *if* the base tag specifies a host that is different than current
 * host. (ajax calls can't be cross-domain).
 *
 * Return string in format:  http://pk.greatschools.org
 */
GS.uri.Uri.getBaseHostname = function() {
    var baseHostname = "";

    if (window.location.hostname.indexOf("pk.") > -1) {
        //"override" any base tag, and point at the current domain
        baseHostname = window.location.protocol + "//" + window.location.host;
    }

    return baseHostname;
};

/**
 * Static method that takes a string that resembles a URL querystring in the format ?key=value&amp;key=value&amp;key=value
 * @param queryString
 * @param key
 * @param value
 */
GS.uri.Uri.putIntoQueryString = function(queryString, key, value, overwrite) {
    queryString = queryString.substring(1);
    var put = false;
    var vars = [];

    if (overwrite === undefined) {
        overwrite = true;
    }

    if (queryString.length > 0) {
        vars = queryString.split("&");
    }

    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split("=");
        var thisKey = pair[0];

        if (overwrite === true && thisKey === key) {
            vars[i] = key + "=" + value;
            put = true;
        }
    }

    if (put !== true) {
        vars.push(key + "=" + value);
    }


    queryString = "?" + vars.join("&");
    return queryString;
};

/**
 * Static method that returns the value associated with a key in the current url's query string
 * @param key
 */
GS.uri.Uri.getFromQueryString = function(key) {
    queryString = decodeURIComponent(window.location.search.substring(1));
    var vars = [];
    var result;

    if (queryString.length > 0) {
        vars = queryString.split("&");
    }

    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split("=");
        var thisKey = pair[0];

        if (thisKey === key) {
            result = pair[1];
            break;
        }
    }
    return result;
};

/**
 * Static method that removes a key/value from the provided querystring
 * @param queryString
 * @param key
 */
GS.uri.Uri.removeFromQueryString = function(queryString, key) {
    if (queryString.substring(0,1) === '?') {
        queryString = queryString.substring(1);
    }
    var vars = [];
    if (queryString.length > 0) {
        vars = queryString.split("&");
    }

    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split("=");
        var thisKey = pair[0];

        if (thisKey == key) {
            // http://wolfram.kriesing.de/blog/index.php/2008/javascript-remove-element-from-array
            vars.splice(i, 1);
            i--;
        }
    }

    queryString = "?" + vars.join("&");
    return queryString;
};

/**
 * Converts URL's querystring into a hash
 * *Warnging: does not work if querystring contains multiple pairs with the same key*
 */
GS.uri.Uri.getQueryData = function() {
    var vars = [], hash;
    var data = {};
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for (var i = 0; i < hashes.length; i++) {
        hash = hashes[i].split('=');
        data[hash[0]] = hash[1];
    }
    return data;
};