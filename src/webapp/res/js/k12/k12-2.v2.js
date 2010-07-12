function setOverlayCookie() {
    subCookie.setObjectProperty('k12Overlay', 'k12Overlay', 1, 365);
}

function hasK12Cookie() {
    return (subCookie.getObject('k12Overlay') != null);
}

function preKillOverlay() {
    document.body.style.background = '#68aaf1 url(/res/img/gs_gradient_bg.jpg) 50% 0 repeat-y';
    document.getElementById('marginGS').style.display = 'block';
}

function postKillOverlay() {
    if (typeof k12Callback == 'function') {
        k12Callback();
    }
}

function initOverlay() {
    if (!hasK12Cookie()) {
        var flashvars = {};
        var params = {wmode:"opaque", allowscriptaccess:"always", salign:"top", bgcolor:"#FFFFFF"};
        var attributes = {};

        document.getElementById('k12Tracker').src = 'http://ad.doubleclick.net/ad/N2942.114495.5898853556421/B4641383;sz=1x1;ord=[timestamp]?';
        swfobject.embedSWF("http://www.greatschools.org/catalog/swf/K12_Overlay_728.swf", "overlay-728-adslot", "100%", "1500", "9.0.115", "http://www.greatschools.org/catalog/swf/expressInstall.swf", flashvars, params, attributes);

        document.body.style.overflow = 'hidden';
        overlaySmallImage = new Image(598, 74);
        overlaySmallImage.src = "http://www.greatschools.org/catalog/images/k12overlay_static728x90.gif";

        setOverlayCookie();
    } else {
        killOverlay();
    }
}
function killOverlay() {
    preKillOverlay();

	swfobject.removeSWF("overlay-728-adslot");
	document.body.style.overflow = 'auto';
	document.getElementById("adHeader_728x90").innerHTML = '<a href="http://ad.doubleclick.net/clk;226334596;50167786;t?http://go.k12.com/wm/tb/cons/k12/200907/orange_a/index.html?se=GREATSCHOOLS&campaign=NAT" target="_new"><img src="http://www.greatschools.org/catalog/images/k12overlay_static728x90.gif" /></a>';

    postKillOverlay();
}

jQuery(document).ready(function() {
    if (typeof GA_googleAddAttr == 'function') {
        initOverlay();
    } else {
        preKillOverlay();
    }
});