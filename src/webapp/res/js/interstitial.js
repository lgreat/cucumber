function doInterstitial() {
    var interstitial = readCookie('gs_interstitial');
    if (!interstitial) {
        for (var i = 0; i < document.links.length; i++) {
            var link = document.links[i];
            if (!isAdLink(link)) {
                link.href = "/ads/interstitial.page?passThroughURI=" + encodeURIComponent(link.href);
            }
        }
    }
}

function isAdLink(link) {
    var adLinkRegExp = new RegExp("googlesyndication|doubleclick|advertising|"+
                       "oascentral|eyewonder|serving-sys|PointRoll|view.atdmt");
    return adLinkRegExp.test(link);
}

