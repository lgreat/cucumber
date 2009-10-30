// JavaScript Document
// required to avoid "$j" collisions with Prototype.js
jQuery.noConflict();
var $j = jQuery;

$j(document).ready(function() {
    $j('#navWrapper #promoTab:not(.on)').hover(function() {
        $j(this).attr('src','/res/img/nav/holiday_nav_on.gif');
    }, function() {
        $j(this).attr('src','/res/img/nav/holiday_nav_off.gif');
    });
});