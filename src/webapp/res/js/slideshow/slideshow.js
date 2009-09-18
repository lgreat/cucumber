// JavaScript Document
// required to avoid "$j" collisions with Prototype.js
jQuery.noConflict();
var $j = jQuery;

/*$j(document).ready(function() {
    $j('#slideMenu').change(function() {
        $j(this).find('option:selected').each(function() {
            window.location.href = $j(this).val();
        });
    });
});*/

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