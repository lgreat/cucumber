//read cookie, return "" if cookie not found or cookie not set
function readEscapedCookie(cookieName) {
    var cookie = "" + document.cookie;
    var i = cookie.indexOf(cookieName);
    if (i == -1 || cookieName == "") return "";

    var j = cookie.indexOf(';', i);
    if (j == -1) j = cookie.length;

    return unescape(cookie.substring(i + cookieName.length + 1, j));
}


/* circumventing browser restrictions on the number of cookies one can use */
var subCookie = {
	nameValueSeparator: '$$:$$',
	subcookieSeparator: '$$/$$',

    /*
     * Gets the entire object
     */
    getObject: function(cookieName){
        //alert("getObject()");
        var cookieValue = readEscapedCookie(cookieName);
        if (cookieValue == undefined || cookieValue.length < 1) {
            //alert("return cookie is null");
            return null;
        }

        var pairs =  cookieValue.split(subCookie.subcookieSeparator);
        var result = new Array();

        //var debug = "";
        for (var i in pairs)    {
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
		for (var i in subcookieObj)
		{
			cookieValue += i + subCookie.nameValueSeparator;
			cookieValue += subcookieObj[i];
			cookieValue += subCookie.subcookieSeparator;
		}
		/* remove trailing subcookieSeparator */
		cookieValue = cookieValue.substring(0,cookieValue.length-subCookie.subcookieSeparator.length);
		createCookie(cookieName,cookieValue,days);
	},

    /*
     * Add or replace a property of the cookie leaving the the other properties intact
     */
    setObjectProperty: function(cookieName, propertyName, propertyValue, days) {
        //alert( "setObjectProperty()");
        var cookieObj = subCookie.getObject(cookieName) ;
        if (cookieObj == undefined){
            //alert("cookieObj is undefined");
            cookieObj = new Array();

        }
        cookieObj[propertyName]  = propertyValue;
        subCookie.setObject(cookieName, cookieObj, days);
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
    }
};