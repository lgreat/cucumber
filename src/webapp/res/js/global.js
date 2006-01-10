/*
Copyright (c) 2006 GreatSchools.net
All Rights Reserved.

$Id: global.js,v 1.1 2006/01/10 18:12:02 apeterson Exp $
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


