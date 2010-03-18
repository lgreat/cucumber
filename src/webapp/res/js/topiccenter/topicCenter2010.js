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

    // GS-9690 - show 160x600 ad (and AD word above the ad), only if ad is running
    if ($j('google_ads_div_Library_Article_Page_AboveFold_Left_160x600').length > 0) {
        $j('.skyscraperAd').show();
    }

    // EOF
});