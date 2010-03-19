// JavaScript Document
// required to avoid "$j" collisions with Prototype.js
jQuery.noConflict();
var $j = jQuery;

$j(document).ready(function() {
    //
    // Initialize (all twirly content closed)
    var theTwirly = $j('#topicbarGS .hasNested');
    //
    // On twirly click
    theTwirly.click(function () {
        ($j('> span', this).hasClass('twirlyClosed')) ? $j('> span', this).removeClass('twirlyClosed').addClass('twirlyOpen') : $j('> span', this).removeClass('twirlyOpen').addClass('twirlyClosed');
        ($j('> span', this).hasClass('twirlyClosed')) ? $j('> ul', this).slideUp(250) : $j('> ul', this).slideDown(250);
        return false;
    });

    // GS-9690 - show 160x600 ad (and AD word above the ad), only if ad is running
    if ($j('google_ads_div_Library_Article_Page_AboveFold_Left_160x600').length > 0) {
        $j('.skyscraperAd').show();
    }
});
// EOF