//read cookie, return "" if cookie not found or cookie not set
function readCookie(cookieName) {
    var cookie = "" + document.cookie;
    var i = cookie.indexOf(cookieName);
    if (i == -1 || cookieName == "") return "";

    var j = cookie.indexOf(';', i);
    if (j == -1) j = cookie.length;

    return unescape(cookie.substring(i + cookieName.length + 1, j));
}

// create a cookie with name=value and optional "days" expiration
function createCookie(name, value, days) {
    var expires = "";
    if (days) {
		var date = new Date();
		date.setTime(date.getTime() + (days*24*60*60*1000));
		expires = "; expires=" + date.toGMTString();
	}

    /* set the domain to empty string if running on a dev machine */
    if (location.hostname == "localhost") {
        var domain = "";
    }else{
        var domain = "; domain=greatschools.net";
    }
    document.cookie = name + "=" + escape(value) + expires + "; path=/" + domain;
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

//get state from cookie
function getState() {
    return readCookie('STATE');
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
    if (textField.value == 'Search by keyword' || textField.value == 'Search for school, district or city'
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
        e.value = 'Search for school, district or city';
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
    if (e.value == 'Search for school, district or city' || e.value == 'Search community by keyword') {
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
    if (e.value == 'Search for school, district or city' || e.value == 'Search by keyword') {
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

/*
 * Used by the global search widget to make sure that a user
 * selects a state.
 */
function checkSearchStateSelected(theForm, selectorId) {
    var returnVal = true;
    var val = document.getElementById(selectorId).value;

    if (val == "--" || val == "") {
        alert("Please select a state.");
        returnVal = false;
    } else {
        returnVal = true;
    }
    return returnVal;
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
        var a = text.replace('\n',' ').replace('\t',' ');
        var z = 0;
        for (; z < a.length; z++) {
            if (a.charAt(z) == ' ' && a.charAt(z-1) != ' ') { count++; }
 	        if (count > max) break;
        }
        
        if (count > max) {
            textField.value = textField.value.substr(0, z);
            var at = alertText;
            if (!at) {
                at = "Please keep your comments to " + max + " words or less."
            }
            alert(at);
            return false;
        }
        return true;
    }
}


function eventTrigger (e) {
    if (! e)
        e = event;
    return e.target || e.srcElement;
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
function registerEventHandler(node, event, handler){
	if (typeof document.addEventListener == "function") {
	  	node.addEventListener(event, handler, false);
	}
	else {
		node.attachEvent("on" + event, handler);
	}
}