//read cookie, return "" if cookie not found or cookie not set
function readCookie(cookieName) {
    var cookie = "" + document.cookie;
    var i = cookie.indexOf(cookieName);
    if (i == -1 || cookieName == "") return "";

    var j = cookie.indexOf(';', i);
    if (j == -1) j = cookie.length;

    return unescape(cookie.substring(i + cookieName.length + 1, j));
}

function hasCookie(cookieName) {
    return (document.cookie.length > 0 && document.cookie.indexOf(cookieName+"=") >= 0);  
}

function getCookieExpiresDate(days,hours,minutes,seconds) {
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
}

// create a cookie with name=value and optional "days" expiration
function createCookieWithExpiresDate(name, value, date) {
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
}

// create a cookie with name=value and optional "days" expiration
function createCookie(name, value, days) {
    if (days) {
        createCookieWithExpiresDate(name, value, getCookieExpiresDate(days));
    } else {
        createCookieWithExpiresDate(name, value);
    }
}

/* Finds the HTML element specified by the ID and switches it between
   block and 'none' display. */
function toggleById(elementId) {
    var layer = document.getElementById(elementId);
    if (layer.style.display == 'block') {
        layer.style.display = 'none';
    } else {
        layer.style.display = 'block';
    }
}

function newToggleById(elementId) {
    var layer = document.getElementById(elementId);
    if (layer.oldDisplay && layout.style.display == 'none') {
        layer.style.display = layer.oldDisplay;
    } else {
        layer.oldDisplay = layer.style.display;
        layer.style.display = 'none';
    }
}

function setColSpan(elementId, colspan) {
    document.getElementById(elementId).colSpan = colspan;
}

function getRadioValue(radioButtons)
{
    for (var i = 0; i < radioButtons.length; i++)
    {
        if (radioButtons[i].checked) {
            return radioButtons[i].value;
        }
    }
}

/*
  GS-site specific
*/
function issues() { window.open("", "issues", 'width=400,height=300,scrollbars=yes') }
function definitions() { window.open("", "issues", 'width=500,height=400,scrollbars=yes') }

function jumpToCounty(newLoc, state) {
    newPage = newLoc.options[newLoc.selectedIndex].text;
    newPage = escape(newPage);
    newPage = "/cgi-bin/search_switch/" + state + "?selector=county&countySelect=" + newPage;
    if (newPage != "") {
        window.location.href = newPage
    }
}

//get user's state from cookie
function getState() {
    return readCookie('STATE3');
}

/*
  Top-nav specific
*/
/*
 * Used by the global search widget to make sure that a user selects a state.
 */
function topNavSubmitSearch(theForm) {
    var val = document.getElementById('stateSelector').value;
    var c = getRadioValue(theForm.c);
    if (c != 'topic' && c != 'community') {
        if (val == "--" || val == "") {
            alert("Please select a state.");
            return false;
        }
    }
    var textField = document.getElementById('q');
    if (textField.value == 'Search by keyword' || textField.value == 'Search for school, district, or city'
            || textField.value == 'Search community by keyword') {
        textField.value = '';
    }
    return true;
}

function topNavSelectSchoolSearch(x, searchFormAction) {
    var e = document.getElementById('stateDropDown');
    e.style.display = 'block';
    e = document.getElementById('stateSelector');
    e.name = 'state';

    e = document.getElementById('q');
    if (e.value == 'Search by keyword' || e.value == 'Search community by keyword') {
        e.value = 'Search for school, district, or city';
    }
    e.style.width = "190px";
    e.focus();
    e.select();
    var searchForm = document.getElementById('topnav_search');
    if (searchForm && searchFormAction) {
        searchForm.action = searchFormAction;
    }
    return true;
}

function topNavSelectTopicSearch(x, searchFormAction) {
    var e = document.getElementById('stateDropDown');
    e.style.display = 'none';
    e = document.getElementById('stateSelector');
    e.name = 'hiddenState';

    e = document.getElementById('q');
    if (e.value == 'Search for school, district, or city' || e.value == 'Search community by keyword') {
        e.value = 'Search by keyword';
    }
    e.style.width = "247px";
    e.focus();
    e.select();
    var searchForm = document.getElementById('topnav_search');
    if (searchForm && searchFormAction) {
        searchForm.action = searchFormAction;
    }
    return true;
}

function topNavSelectCommunitySearch(x, searchFormAction) {
    var e = document.getElementById('stateDropDown');
    e.style.display = 'none';
    e = document.getElementById('stateSelector');
    e.name = 'hiddenState';

    e = document.getElementById('q');
    if (e.value == 'Search for school, district, or city' || e.value == 'Search by keyword') {
        e.value = 'Search community by keyword';
    }
    e.style.width = "247px";
    e.focus();
    e.select();
    var searchForm = document.getElementById('topnav_search');
    if (searchForm && searchFormAction) {
        searchForm.action = searchFormAction;
    }
    return true;
}

/*
 * Used by the global search widget to make sure that a user selects a state.
 */
function topNavNewCommunitySubmitSearch(theForm) {
    var val = document.getElementById('stateSelector').value;
    var articlesAndCommunity = false;
    var c = getRadioValue(theForm.c);
    if (c != 'articlesAndCommunity') {
        if (val == "--" || val == "") {
            alert("Please select a state.");
            return false;
        }
        articlesAndCommunity = false;
    } else {
        articlesAndCommunity = true;
    }
    var textField = document.getElementById('qNew');
    if (textField.value == 'Search by keyword' || textField.value == 'Search for school, district, or city') {
        textField.value = '';
    }
    if (articlesAndCommunity) {
        var q = document.getElementById('articlesAndCommunityQ');
        q.value = textField.value;
        document.getElementById('topnav_search_articlesAndCommunity').submit();
    } else {
        document.getElementById('topnav_search').submit();
    }

    return false;
}

function topNavNewCommunitySelectSchoolSearch(x, searchFormAction) {
    var e = document.getElementById('stateSelector');
    e.style.display = 'inline';
    e.name = 'state';

    e = document.getElementById('qNew');
    if (e.value == 'Search by keyword' || e.value == 'Search community by keyword') {
        e.value = 'Search for school, district, or city';
    }
    e.style.width = "242px";
    e.focus();
    e.select();
    var searchForm = document.getElementById('topnav_search');
    if (searchForm && searchFormAction) {
        searchForm.action = searchFormAction;
    }
    return true;
}

function topNavSelectArticlesAndCommunitySearch(x, searchFormAction) {
    var e = document.getElementById('stateSelector');
    e.style.display = 'none';
    e.name = 'hiddenState';

    e = document.getElementById('qNew');
    if (e.value == 'Search for school, district, or city') {
        e.value = 'Search by keyword';
    }
    e.style.width = "300px";
    e.focus();
    e.select();
    var searchForm = document.getElementById('topnav_search');
    if (searchForm && searchFormAction) {
        searchForm.action = searchFormAction;
    }
    return true;
}

/* Sets the search prompt in the global header */
function setSearchPrompt(s) {

    var d = document.getElementById('slabel');
    var olddiv = document.getElementById('searchPrompt');
    d.removeChild(olddiv);

    var newdiv = document.createElement('img');
    newdiv.setAttribute('id', 'searchPrompt');

    if (s == 'Enter keyword') {
        newdiv.setAttribute('src', '/res/img/search/enter_keyword.gif');
        newdiv.setAttribute('alt', 'Enter Keyword')
    } else {
        newdiv.setAttribute('src', '/res/img/search/enter_school.gif');
        newdiv.setAttribute('alt', 'Enter School, City or District')
    }

    d.appendChild(newdiv);

    var e = document.getElementById("q");
    e.focus();
    e.select();
}

//inspired by http://www.thewatchmakerproject.com/journal/308/equal-height-boxes-with-javascript
var BoxHeights = {
	equalize: function() {
        var maxH = 0;
        var numCols = arguments.length;
        var column = new Array();

        for (var i=0;i<numCols;i++) {
            var id = document.getElementById(arguments[i]);
            if (id) {
                column.push(id);
                var curHeight = 0;
                curHeight = (navigator.userAgent.toLowerCase().indexOf('opera') == -1) ? id.scrollHeight : id.offsetHeight;
                if (curHeight > maxH) maxH = curHeight;
            } else {
                return;  //early exit.
            }
        }
        numCols = column.length;
        for (var i=0;i<numCols;i++) column[i].style.height = maxH+"px";
	}
};

/*
 * Returns a function that counts the words in a string.
 * Params:
 *     max - the max number of words allowed in the string.
 *     alertText - [optional] - the text displayed in an alert() if
 *                 the max is reached.
 *
 * The returned function accepts a single argument containing the
 * the textField with the words to be counted - usually a <textarea>.
 */
function makeCountWords(max, alertText) {
    return function (textField) {
        var text = textField.value;
        var count = 0;
        var a = text.replace(/\n/g,' ').replace(/\t/g,' ');
        var z = 0;
        for (; z < a.length; z++) {
            if (a.charAt(z) == ' ' && a.charAt(z-1) != ' ') { count++; }
 	        if (count > max) break;
        }
        
        if (count > max) {
            textField.value = textField.value.substr(0, z);
            var at = alertText;
            if (!at) {
                at = "Please keep your comments to " + max + " words or less.";
            }
            alert(at);
            return false;
        }
        return true;
    };
}


function eventTrigger (e) {
    if ( e == null || ! e)
        e = event;
    return e.target || e.srcElement;
}

function getNode(e){

     var obj = eventTrigger(e);

     // if obj is an image get it's parent (expected to be a link)
     if (obj.tagName.toLowerCase() == 'img' || obj.tagName.toLowerCase() == 'span') {
         obj = obj.parentNode;
     }

     return obj;
 }
/*
 * registers a dom event handler
 *
 * params
 *    node    DOM node that the handler should be attached to
 *    event   the name of the event type, such as "click" or "keypress"
 *    handler the handler function
 * example
 *    var node = document.getElementById("My-Node-Id");
 *    registerEventHandler(node,"click",handleCI9Info);
 */
function registerEventHandler(node, eventType, handler){
    if (node.addEventListener){
        node.addEventListener(eventType, handler, false);
        return true;
    } else if (node.attachEvent){
        var r = node.attachEvent("on"+eventType, handler);
        return r;
    } else {
        //alert("Handler could not be attached");
    }
}

// Swaps text value with another
function GS_textSwitch(el, target, replace) {
    if (el.value == replace) {
        el.value = target;
    }
}

function GS_addPledgeCookie() {
    createCookie('pledged', '1', 730);
}

if (GS == undefined) {
    var GS = {};
}

GS.closeDialogHandlers = [];
GS.registerCloseDialogHandler = function(f) {
    GS.closeDialogHandlers.push(f);
};
GS.closeDialogs = function() {
    for (var i=0; i < GS.closeDialogHandlers.length; i++) {
        GS.closeDialogHandlers[i]();
    }
};

//read cookie, return "" if cookie not found or cookie not set
function readEscapedCookie(cookieName) {
    var cookie = "" + document.cookie;
    var i = cookie.indexOf(cookieName);
    if (i == -1 || cookieName == "") return "";

    var j = cookie.indexOf(';', i);
    if (j == -1) j = cookie.length;
    var value = cookie.substring(i + cookieName.length + 1, j) ;

    return unescape(value);
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
		createCookie(cookieName,cookieValue,days);
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

GS_userIsLoggedIn = function(cookieName){
    // if cookie MEMID exists, the user is logged in
    var value = readCookie(cookieName);

    return value != undefined && value.length > 0;
};
