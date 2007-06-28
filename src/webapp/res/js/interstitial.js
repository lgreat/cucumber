function doInterstitial() {
    var interstitial = readCookie('gs_interstitial');
    if (!interstitial && !isAdFree()) {
        for (var i = 0; i < document.links.length; i++) {
            var link = document.links[i];
            if (!isAdLink(link)) {
                link.href = "/ads/interstitial.page?passThroughURI=" + encodeURIComponent(link.href);
            }
        }
        createCookie('gs_interstitial', 'viewed', 1);
    }
}

function isAdLink(link) {
    // todo
    return false;
}

function isAdFree() {
    var adFreeRegExp = new RegExp("mcguire|framed|number1expert|vreo|e-agent|homegain|envirian|connectingneighbors");
    return adFreeRegExp.test(document.URL);
}

/** Returns true if the user-agent is a known crawler. */
function isCrawler() {
    var uaRegExp = new RegExp(".*(googlebot|mediapartners-google|slurp|mmcrawler|msnbot|teoma|ia_archiver).*");
    return uaRegExp.test(window.navigator.userAgent.toLowerCase());
}

function createCookie(name,value,days) {
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}