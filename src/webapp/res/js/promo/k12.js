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

    jQuery('#SponsoredSearch_Top_423x68 .jq-k12TrafficDriver, #SponsoredSearch_Bottom_423x68 .jq-k12TrafficDriver, #TopRatedSponsor_310x40 .jq-k12TrafficDriver, #House_Ad_370x158 .jq-k12TrafficDriver, #CustomSponsor_407x65 .jq-k12TrafficDriver, #Sponsor_610x16 .jq-k12TrafficDriver').
            unbind('click').click(function() {

        var href = jQuery(this).attr('href');
        var cssClass = jQuery(this).attr('class');
        if (href != null && cssClass != null) {
            var classList = GS.promo.K12.trafficDriver.getCssClassList(cssClass);
            var gamStateAttr = GS.promo.K12.trafficDriver.getGamStateAttr(classList);
            var k12School = GS.promo.K12.trafficDriver.getK12School(gamStateAttr);
            var k12TrafficDriver = GS.promo.K12.trafficDriver.getK12TrafficDriver(classList);

            var hostname = document.URL.getHostname();

            if (s.tl) {
                s.tl(true, 'o', 'K12_TrafficDriver_' + k12School);
            }

            var encodedClickThroughUrl =
                encodeURIComponent('http://' + hostname + '/online-education.page?school=' + k12School + '&t=' + k12TrafficDriver);
            window.open(href + encodedClickThroughUrl, '_blank');
            return false;
        }

    });
});