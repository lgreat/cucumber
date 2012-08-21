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
GS.uri.Uri.getFromQueryString = function(key, queryString) {
    queryString = queryString || decodeURIComponent(window.location.search.substring(1));
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
 * Now works with queryStrings that contain multiple key=value pairs with the same key
 */
GS.uri.Uri.getQueryData = function(queryString) {
    var vars = [], hash;
    var data = {};
    if(queryString !== undefined) {
        queryString = queryString.substring(1);
    }
    else {
        var index = window.location.href.indexOf('?');
        if(index === -1) {
            queryString = "";
        }
        else {
            queryString = window.location.href.slice(index + 1);
        }
    }

    var hashes = queryString.split('&');
    if (queryString.length > 0 && hashes.length > 0) {
        for (var i = 0; i < hashes.length; i++) {
            var hash = hashes[i].split('=');
            var key = hash[0];
            var value = hash[1];

            // if the querystring key is already in the data hash, then the querystring had multiple key=value pairs
            // with the same key. Make the key point to an array with all the values
            if (data.hasOwnProperty(key)) {
                // if the value in the data hash is _already_ an array, just push on the value
                if (data[key] instanceof Array) {
                    data[key].push(value);

                    // otherwise we need to copy the existing value that's on the data hash into a new array
                } else {
                    var anArray = [];
                    anArray.push(data[key]);
                    anArray.push(hash[1]);
                    data[hash[0]] = anArray;
                }
            } else {
                data[hash[0]] = hash[1];
            }
        }
    }
    return data;
};

GS.uri.Uri.getQueryStringFromObject = function(obj) {
    var queryString = '';
    for (var key in obj) {
        if (obj.hasOwnProperty(key)) {
            var value = obj[key];
            if (value instanceof Array) {
                for (var i = 0; i < value.length; i++) {
                    if (queryString.length > 0) {
                        queryString = queryString + '&';
                    }
                    queryString = queryString + key + '=' + value[i];
                }
            } else if (value instanceof Object) {
                if (queryString.length > 0) {
                    queryString = queryString + '&';
                }
                queryString = queryString + GS.uri.Uri.getQueryStringFromObject(value); // recursion
            } else {
                if (queryString.length > 0) {
                    queryString = queryString + '&';
                }
                queryString = queryString + key + '=' + value;
            }
        }
    }

    if (queryString !== '') {
        return '?' + queryString;
    }

    return '';
};

// TODO: move to different file
// overwrites all keys in obj1 into obj2, and overwrites if desired. Does not perform deep traversal
// Returns obj2
GS.uri.Uri.mergeObjectInto = function(obj1, obj2, overwrite) {

    for (var key in obj1) {
        if (obj1.hasOwnProperty(key)) {
            if (overwrite === true || !obj2.hasOwnProperty(key)) {
                obj2[key] = obj1[key];
            }
        }
    }

    return obj2;
};