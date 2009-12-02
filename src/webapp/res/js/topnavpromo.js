// JavaScript Document

jQuery(document).ready(function() {
    jQuery('#navWrapper #promoTab:not(.on)').hover(function() {
        jQuery(this).attr('src','/res/img/nav/holiday_nav_on.gif');
    }, function() {
        jQuery(this).attr('src','/res/img/nav/holiday_nav_off.gif');
    });
});