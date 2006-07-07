/*
Copyright (c) 2006 GreatSchools.net
All Rights Reserved.

$Id: global.js,v 1.3 2006/07/07 23:07:31 apeterson Exp $
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


