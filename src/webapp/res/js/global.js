/*
Copyright (c) 2006 GreatSchools.net
All Rights Reserved.

$Id: global.js,v 1.2 2006/06/26 21:27:50 apeterson Exp $
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



