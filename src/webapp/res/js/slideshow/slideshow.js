// JavaScript Document
// required to avoid "$j" collisions with Prototype.js
var $j = jQuery;
var showNlHover = false;
$j(document).ready(function() {

    var cookie = subCookie.getObjectPropertyIfNotExpired("all_hover", "showNLHoverOnArticles");
    var fromNewsletter = $j('#cpnCodeFromNewsletter').val();

    if (cookie == null && fromNewsletter === '') {
        showNlHover = true;
    }

    var type = $j("#cmsContentType").val();
    var isShowNlSubHover = $j("#isShowNlSubHover").val();
    var socialButtons = $j('.articleDistribution:last');

    if (showNlHover && type == 'articleSlideshow' && isShowNlSubHover == 'true') {
        var slideNum = $j("#articleSlideshowNum").val();
        if (slideNum == 2) {
            GSType.hover.nlSubscription.showHover();
        }
    }

    $j(window).scroll(function() {
        if (showNlHover && type == 'article' && isShowNlSubHover == 'true') {
            var reachedScrollPositionToSocialIcons = isScrolledIntoView(socialButtons);
            if (reachedScrollPositionToSocialIcons) {
                GSType.hover.nlSubscription.showHover();
                showNlHover = false;
            }
        }
    });
});

function isScrolledIntoView(elem) {
    var docViewTop = $j(window).scrollTop();
    var docViewBottom = docViewTop + $j(window).height();

    var elemTop = elem.offset().top;
    var elemBottom = elemTop + elem.height();

    return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom));
}

var timeout = 500;
var closetimer = 0;
var ddmenuitem = 0;

function ss_jsddm_open() {
    ss_jsddm_canceltimer();
    ss_jsddm_close();
    ddmenuitem = $j(this).find('ul').eq(0).css('visibility', 'visible');
}

function ss_jsddm_close() {
    if (ddmenuitem) ddmenuitem.css('visibility', 'hidden');
}

function ss_jsddm_timer() {
    closetimer = window.setTimeout(ss_jsddm_close, timeout);
}

function ss_jsddm_canceltimer() {
    if (closetimer) {
        window.clearTimeout(closetimer);
        closetimer = null;
    }
}

$j(document).ready(function() {
    $j('#ss_jsddm > li').bind('mouseover', ss_jsddm_open);
    $j('#ss_jsddm > li').bind('mouseout', ss_jsddm_timer);
});

document.onclick = ss_jsddm_close;