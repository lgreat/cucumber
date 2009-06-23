// JavaScript Document
// required to avoid "$j" collisions with Prototype.js
jQuery.noConflict();
var $j = jQuery;

$j(document).ready(function() { 
	
	// Initialize state
    var i = 0;
    $j('.topic').hide();
    $j('.topic:first').show();
	// EOF
});