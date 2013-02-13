// requires omniture script

var pageTracking = {
    pageName: "",
    server: "",
    hierarchy: "",
    successEvents: "",
    eVars: {},
    props: {},

    send: function(){
        s.pageName = this.pageName;
        s.server = this.server;
        s.hier1 = this.hierarchy;
        s.events = this.successEvents;
        for (var evar in this.eVars){
            s[evar] = this.eVars[evar];
        }
        for (var prop in this.props){
            s[prop] = this.props[prop];
        }
        var s_code=s.t();
        if(s_code){
            document.write(s_code);
        }
    },
    clear: function() {
        this.pageName = "";
        this.hierarchy = "";
        this.successEvents = "";
        this.eVars = {};
        this.props = {};
    }
};

var GS = GS || {};
GS.tracking = GS.tracking || {};
GS.tracking.data = GS.tracking.data || {};
GS.tracking.registerTrackingData = function(key, data) {
    // copy data from provided data hash into GS.tracking.data. Only copy values not equal to empty string

    if (GS.tracking.data[key] === undefined) {
        GS.tracking.data[key] = {};
        GS.tracking.data[key].props = {};
    }

    var destination = GS.tracking.data[key];

    if (data.props !== undefined) {
        var propData = data.props;
        for (var p in propData) {
            if (propData.hasOwnProperty(p)) {
                if (propData[p] !== '') {
                    destination.props[p] = propData[p];
                }
            }
        }
    }

    for (var k in data) {
        if (data.hasOwnProperty(k)) {
            var item = data[k];
            if (item !== '' && k !== 'props') {
                destination[k] = item;
            }
        }
    }
};
GS.tracking.sendOmnitureData = function(key, preserveSuccessEvents) {
    var data = GS.tracking.data[key];
    var sharedData = GS.tracking.data['_shared'];
    GS.tracking.removeTabSpecificProps(key, data);
    if (data !== undefined) {
        pageTracking.clear();
        $.extend(pageTracking, data);
        if (sharedData !== undefined) {
            $.extend(true, pageTracking, sharedData);
        }
        if (preserveSuccessEvents && s.events != '') {
            if (pageTracking.successEvents != '') {
                pageTracking.successEvents = pageTracking.successEvents + ";" + s.events;
            } else {
                pageTracking.successEvents = s.events;
            }
        }
        pageTracking.send();
    }
};

GS.tracking.tabSpecificPropsMap = GS.tracking.tabSpecificPropsMap || {};
GS.tracking.addTabSpecificProps = function(tabName, propNumber) {
    if(GS.tracking.tabSpecificPropsMap[tabName] === undefined) {
        GS.tracking.tabSpecificPropsMap[tabName] = {};
        GS.tracking.tabSpecificPropsMap[tabName].props = [];
    }

    var propsMap = GS.tracking.tabSpecificPropsMap[tabName].props;

    if(propNumber !== undefined) {
        propsMap.push(propNumber);
    }
};

GS.tracking.removeTabSpecificProps = function(currentTab, data) {
    for(var tab in GS.tracking.tabSpecificPropsMap) {
        if(tab !== currentTab && GS.tracking.tabSpecificPropsMap.hasOwnProperty(tab) && GS.tracking.tabSpecificPropsMap[tab].props !== undefined) {
            for(var i in GS.tracking.tabSpecificPropsMap[tab].props) {
                var prop = GS.tracking.tabSpecificPropsMap[tab].props[i];
                if(prop !== undefined) {
                    data.props[prop] = '';
                }
            }
        }
    }
};