/*
Copyright (c) 2006 GreatSchools.net
All Rights Reserved.

$Id: global.js,v 1.6 2006/07/26 21:49:16 dlee Exp $
*/

/* Finds the HTML element specified by the ID and switches it between
   block and 'none' display. Should not be used on other types of elements
   as its behavior is not defined. */
function toggleById(elementId) {
	var layer = document.getElementById(elementId);

	if (layer.style.display == 'block') {
		layer.style.display = 'none';
	} else {
		layer.style.display = 'block';
	}
}

/* Sets the search prompt in the global header */
function setSearchPrompt(s) {
    var e = document.getElementById("searchPrompt");
    e.innerHTML = s;
    var e = document.getElementById("q");
    e.focus();
    e.select();
}



/* From the old perl code. Probably not needed */
function issues(){
    window.open("","issues",'width=400,height=300,scrollbars=yes')
}
function definitions(){
    window.open("","issues",'width=500,height=400,scrollbars=yes')
}
function jumpCounty(newLoc) {
newPage=newLoc.options[newLoc.selectedIndex].text;
newPage=escape(newPage);
newPage="/cgi-bin/search_switch/"+"$STATE"+"?selector=county&countySelect="+
newPage;
if(newPage !="") {window.location.href=newPage}
}

/*
 * Used by the global search widget to make sure that a user
 * selects a state.
 */
function checkSearchStateSelected(selectorId) {
    var val = document.getElementById(selectorId).value;
    var returnVal;
    if (val == "--" || val == "") {
        alert ("Please select a state.");
        returnVal = false;
    } else {
        returnVal = true;
    }
    return returnVal;
}

//get element by id
function getElement(id) {
	return document.getElementById(id);
}

//read cookie, return "" if cookie not found or cookie not set
function readCookie(cookieName) {
    var cookie=""+document.cookie;
    var i=cookie.indexOf(cookieName);
    if (i==-1 || cookieName=="") return "";
    var j=cookie.indexOf(';',i);
    if (j==-1) j=cookie.length;
    return unescape(cookie.substring(i+cookieName.length+1,j));
}

//get state from cookie
function getState() {
    return readCookie('state');
}