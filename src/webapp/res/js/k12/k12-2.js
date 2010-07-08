function setOverlayCookie() {
    subCookie.setObjectProperty('k12Overlay', 'k12Overlay', 1, 365);
}

function initOverlay() {
	var flashvars = {};
	var params = {wmode:"opaque", allowscriptaccess:"always", salign:"top", bgcolor:"#FFFFFF"};
	var attributes = {};

	swfobject.embedSWF("http://www.greatschools.org/catalog/swf/K12_Overlay_728.swf", "overlay-728-adslot", "100%", "1500", "9.0.115", "http://www.greatschools.org/catalog/swf/expressInstall.swf", flashvars, params, attributes);

	document.body.style.overflow = 'hidden';
	overlaySmallImage= new Image(598,74);
	overlaySmallImage.src="http://www.greatschools.org/catalog/images/k12overlay_static728x90.gif";

    setOverlayCookie();
}
function killOverlay() {
    document.body.style.background = '#68aaf1 url(/res/img/gs_gradient_bg.jpg) 50% 0 repeat-y';
    document.getElementById('marginGS').style.display = 'block';

	swfobject.removeSWF("overlay-728-adslot");
	document.body.style.overflow = 'auto';
	document.getElementById("adHeader_728x90").innerHTML = '<a href="http://ad.doubleclick.net/clk;226334596;50167786;t?http://go.k12.com/wm/tb/cons/k12/200907/orange_a/index.html?se=GREATSCHOOLS&campaign=NAT" target="_new"><img src="http://www.greatschools.org/catalog/images/k12overlay_static728x90.gif" /></a>';
}

if (typeof GA_googleAddAttr == 'function') {
    swfobject.addDomLoadEvent(initOverlay);
} else {
    jQuery(document).ready(function() {
        document.body.style.background = '#68aaf1 url(/res/img/gs_gradient_bg.jpg) 50% 0 repeat-y';
        document.getElementById('marginGS').style.display = 'block';
    });
}