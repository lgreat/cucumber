GS = GS || {};
GS.search = GS.search || {};





jQuery(function () {
if (jQuery.browser.msie && jQuery.browser.version.substr(0,1)<7) {
jQuery('.js-trigger,.js-popup').mouseover(function(){
    // do something
    jQuery('#sort-by').hide();
    jQuery('#page-size').hide();
}).mouseout(function(){
    // do something else
    jQuery('#sort-by').show();
    jQuery('#page-size').show();
})
}
});



