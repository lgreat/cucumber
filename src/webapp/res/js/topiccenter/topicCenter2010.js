// JavaScript Document
// required to avoid "$j" collisions with Prototype.js
jQuery.noConflict();
var $j = jQuery;

$j(document).ready(function() {
    //
    // Initialize (all twirly content closed)
    var theTwirly = $j('ul.text2bold li span');
    //
    // On twirly click
    theTwirly.click(function () {
        ($j(this).hasClass('twirlyClosed')) ? $j(this).removeClass('twirlyClosed').addClass('twirlyOpen') : $j(this).removeClass('twirlyOpen').addClass('twirlyClosed');
        ($j(this).hasClass('twirlyClosed')) ? $j(this).parent().find('ul').slideUp(250) : $j(this).parent().find('ul').slideDown(250);
    });
    // EOF
});