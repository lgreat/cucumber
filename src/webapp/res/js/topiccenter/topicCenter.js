// JavaScript Document
// required to avoid "$j" collisions with Prototype.js
jQuery.noConflict();
var $j = jQuery;

$j(document).ready(function() {
	
	// Initialize state
    var i = 0;
    $j('.topic').hide();
    $j('.topic:first').show();
    var whichTopic = $j('.topic:first').index(this);
    //console.log('whichTopic = ' + whichTopic);
    $j('#topicNavigation li:first').addClass("highlight");

    // Set div to show on click
    $j('#topicNavigation li').click(function () {
        var whichListItem = $j('#topicNavigation li').index(this);
        //console.log('whichListItem = ' + whichListItem);
        $j('#topics .topic').hide();
        $j('#topics .topic').eq(whichListItem).show();
        $j('#topicNavigation li').removeClass("highlight");
        $j(this).addClass("highlight");
    });

	// EOF
});