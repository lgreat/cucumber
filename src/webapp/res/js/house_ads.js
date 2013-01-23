String.prototype.startsWith = function(str){
    return (this.indexOf(str) === 0);
}

// http://beardscratchers.com/journal/using-javascript-to-get-the-hostname-of-a-url
String.prototype.getHostname = function() {
    var re = new RegExp('^(?:f|ht)tp(?:s)?\://([^/]+)', 'im');
    return this.match(re)[1].toString();
}

Function.prototype.k12_traffic_driver_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};

GS = GS || {};

GS.ad = GS.ad || {};
GS.ad.hideGhostTextForSingleAd = function(wrapperDivId, isGptAsync, frameElem) {
    var wrapperDiv = jQuery('#' + wrapperDivId);
    if (isGptAsync) {
        if (frameElem != null) {
            // regular GPT Async
            jQuery(frameElem).siblings('.jq-ghostText').hide();
            // GPT Async with disableGptGhostTextHiding enabled for this ad slot
            jQuery(frameElem).parent().siblings('.jq-ghostText').hide();
        }
    } else {
        // GPT Sync
        wrapperDiv.parent().siblings('.jq-ghostText').hide();
    }
    wrapperDiv.show();
};

GS.ad.unhideGhostTextForAdSlots = function(adSlots) {
    var i, slotName, frameElem, jqFrameElem;

    for (i = adSlots.length - 1; i >= 0; i--) {
        slotName = adSlots[i];
        // the _0 at the end assumes we only have one instance of the same ad slot on the page; GPT allows multiple instances but we don't use that feature yet
        frameElem = document.getElementById('google_ads_iframe_/1002894/' + slotName + '_0');
        if (frameElem != null) {
            jqFrameElem = jQuery(frameElem);

            // regular GPT Async - this should never be needed because ghost text would get wiped out on an ad refresh anyway, without calling disableGptGhostTextHiding
            //jqFrameElem.siblings('.jq-ghostText').show();

            // GPT Async with disableGptGhostTextHiding enabled for this ad slot
            jqFrameElem.parent().siblings('.jq-ghostText').show();
        }
    }
};


GS.promo = GS.promo || {};
GS.promo.K12 = GS.promo.K12 || {};

GS.promo.K12.writeNameHelper = function(key) {
    if (typeof GS.promo.K12[key] !== 'undefined') {
        document.write(GS.promo.K12[key]);
    }
};

GS.promo.K12.writeStateName = function() {
    GS.promo.K12.writeNameHelper('k12StateName');
};

GS.promo.K12.writeSchoolName = function() {
    GS.promo.K12.writeNameHelper('k12SchoolName');
};

GS.promo.K12.TrafficDriver = function() {

    this.OTHER_TRAFFIC_DRIVER = 'ot';

    this.getCssClassList = function(cssClass) {
        if (cssClass != null) {
            return cssClass.split(/\s+/);
        }
        return [];
    };

    this.getGamStateAttr = function(classList) {
        for (var i = 0, classListLength = classList.length; i < classListLength; i++) {
            if (classList[i].startsWith('k12s-')) {
                var stateAttr = classList[i].replace(/^k12s-/, '');
                return (stateAttr !== '' ? stateAttr : null);
            }
        }
        return null;
    };

    this.isK12State = function(stateAbbrev) {
        var k12StatePattern = /AR|AZ|CA|CO|DC|FL|GA|HI|ID|IL|IN|KS|MN|MO|NV|OH|OK|OR|PA|SC|TX|UT|WA|WI|WY/;
        return (stateAbbrev != null && stateAbbrev.match(k12StatePattern) != null);
    };

    this.getK12School = function(gamStateAttr) {
        if (this.isK12State(gamStateAttr)) {
            return gamStateAttr;
        }
        return 'INT';
    }.k12_traffic_driver_bind(this);

    this.getK12TrafficDriver = function(classList) {
        for (var i = 0, classListLength = classList.length; i < classListLength; i++) {
            if (classList[i].startsWith('k12t-')) {
                var k12TrafficDriver = classList[i].replace(/^k12t-/, '');
                if (k12TrafficDriver != null && k12TrafficDriver.length === 2) {
                    return k12TrafficDriver;
                } else {
                    return this.OTHER_TRAFFIC_DRIVER;
                }
            }
        }
        return this.OTHER_TRAFFIC_DRIVER;
    }.k12_traffic_driver_bind(this);
};

jQuery(function() {

    GS.promo.K12.trafficDriver = GS.promo.K12.trafficDriver || new GS.promo.K12.TrafficDriver();

    jQuery('.jq-k12TrafficDriver').unbind('click').click(function() {

            var href = jQuery(this).attr('href');
            var cssClass = jQuery(this).attr('class');
            if (href != null && cssClass != null) {
                var classList = GS.promo.K12.trafficDriver.getCssClassList(cssClass);
                var gamStateAttr = GS.promo.K12.trafficDriver.getGamStateAttr(classList);
                var k12School = GS.promo.K12.trafficDriver.getK12School(gamStateAttr);
                var k12TrafficDriver = GS.promo.K12.trafficDriver.getK12TrafficDriver(classList);

                var hostname = document.URL.getHostname();

                var encodedClickThroughUrl =
                    encodeURIComponent('http://' + hostname + '/online-education.page?school=' + k12School + '&t=' + k12TrafficDriver);
                window.open(href + encodedClickThroughUrl, '_blank');
                return false;
            }

        });

    jQuery('#js-k12Form-fs').unbind('submit').submit(function() {
        var selectedState = jQuery('#js-k12StateSelect-fs').val();
        var k12School = GS.promo.K12.trafficDriver.getK12School(selectedState);

        if (s.tl) {
            s.tl(true, 'o', 'K12_TrafficDriver_' + k12School);
        }

        var link="/online-education.page?school=" + k12School + "&t=fs";
        window.open(link, '_blank');
        return false;
    });
});