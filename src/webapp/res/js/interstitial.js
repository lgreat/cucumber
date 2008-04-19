function isBlank(x) {
    return x == undefined || x == null || x == '';
}

function makeInterstitialHref(passThroughHref, adSlot) {
    var href = 'http://' + location.host + "/ads/interstitial.page?";
    if (adSlot) {
        href += "adslot=" + adSlot + "&"
    }
    href += "passThroughURI=" + encodeURIComponent(passThroughHref);
    return href;
}


/**
 * Form inputs that trigger the interstitial must be explicitly *included* by adding
 * a 'goInterstitial' class - as opposed to <a> tags which must be explicitly *excluded*
 * by adding a 'noInterstitial' class.
 */
function makeInterstitialOnSubmit(adSlot) {

    return function (f) {
        var path = this.action + "?";
        var inputs = this.getElementsByTagName('input');
        for (var i = 0; i < inputs.length; i++) {
            if (!isBlank(inputs[i].name)) {
                if (isBlank(inputs[i].value)) {
                    return true;
                }
                path += inputs[i].name + "=" + inputs[i].value + "&";
            }
        }

        var selects = this.getElementsByTagName('select');
        for (var i = 0; i < selects.length; i++) {
            if (!isBlank(selects[i].name)) {
                if (isBlank(selects[i].value)) {
                    return true;
                }
                path += selects[i].name + "=" + selects[i].value + "&";
            }
        }

        path = path.replace(/&$/, '');
        window.location = makeInterstitialHref(path, adSlot);
        return false;
    }
}
/**
 * adSlot is an option argument which specifies a google adslot. @see GS-6114
  */
function doInterstitial(adSlot) {
    var interstitial = readCookie('gs_interstitial');
    if (!interstitial) {
        for (var i = 0; i < document.links.length; i++) {
            var link = document.links[i];
            if (!isAdLink(link) && !isExcludedLink(link)) {
                link.href = makeInterstitialHref(link.href, adSlot);
            }
        }

        var forms = document.getElementsByTagName('form');
        for(var i = 0; i < forms.length; i++) {
            if (forms[i].className.match(/goInterstitial/)) {
                forms[i].onsubmit = makeInterstitialOnSubmit(adSlot);
            }
        }
    }
}

function isAdLink(link) {
    var adLinkRegExp = new RegExp("googlesyndication|doubleclick|advertising|"+
                       "oascentral|eyewonder|serving-sys|PointRoll|view.atdmt");
    return adLinkRegExp.test(link) || (link.target == "_blank");
}

function isExcludedLink(link) {
    return link.className.match(/noInterstitial/);
}
