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
    // todo ultimately the server side will need to check if the .js file should be included, we don't want to maintain this list in yet another place
    // todo ad free cobrands should not get the interstitial, for that matter I'm guessing no cobrands should because we don't serve popunders to cobrands
    var adFreeRegExp = new RegExp("mcguire|framed|number1expert|vreo|e-agent|homegain|envirian|connectingneighbors");
    return adFreeRegExp.test(document.URL);
}

/** Returns true if the user-agent is a known crawler. */
function isCrawler() {
    // todo same as the above, ultimately this check should happen server side and the .js file should not be included or run for cralwers
    var uaRegExp = new RegExp(".*(googlebot|mediapartners-google|slurp|mmcrawler|msnbot|teoma|ia_archiver).*");
    return uaRegExp.test(window.navigator.userAgent.toLowerCase());
}

function createCookie(name,value,days) {
    // todo this should be a domain cookie, not a host cookie.
    if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}