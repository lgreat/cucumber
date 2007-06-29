function doInterstitial() {
    var interstitial = readCookie('gs_interstitial');
    if (!interstitial && !isCrawler()) {
        for (var i = 0; i < document.links.length; i++) {
            var link = document.links[i];
            if (!isAdLink(link)) {
                link.href = "/ads/interstitial.page?passThroughURI=" + encodeURIComponent(link.href);
            }
        }
    }
}

function isAdLink(link) {
    var adLinkRegExp = new RegExp("googlesyndication|doubleclick");
    return adLinkRegExp.test(link);
}

function isCrawler() {
    var uaRegExp = new RegExp(".*(googlebot|mediapartners-google|slurp|mmcrawler|msnbot|teoma|ia_archiver).*");
    return uaRegExp.test(window.navigator.userAgent.toLowerCase());
}
