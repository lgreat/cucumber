/*
Copyright (c) 2006 GreatSchools.net
All Rights Reserved.

$Id: global.js,v 1.4 2006/07/25 00:47:24 chriskimm Exp $
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
function checkSearchStateSelected() {
    var val = document.getElementById('stateSelector').value;
    var returnVal;
    if (val == "--" || val == "") {
        alert ("Please select a state.");
        returnVal = false;
    } else {
        returnVal = true;
    }
    return returnVal;
}