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

var omnitureEventNotifier = omnitureEventNotifier || pageTracking;

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
    if (data !== undefined) {
        GS.tracking.removeTabSpecificProps(key, data);
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
    if (currentTab === undefined || data === undefined) {
        return;
    }
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

/**
 * Based off of:
 * http://stackoverflow.com/questions/7692746/javascript-omniture-how-to-clear-all-properties-of-an-object-s-object
 */
GS.tracking.clearSvariable = function(sObject) {
    var maxPropsAndEvars = 75; // Omniture currently gives us a max of 75 props and evars
    var svarArr = ['pageName','channel','products','events','campaign','purchaseID','state','zip','server','linkName'];
    var i;

    i = maxPropsAndEvars;
    while (i--) {
        sObject['prop'+i]='';
        sObject['eVar'+i]='';
        if(i <= 5) {
            sObject['hier'+i]='';
        }
    }

    i = svarArr.length;
    while (i--) {
        sObject[svarArr[i]]='';
    }
};

GS.tracking.sendAndRestore= function(trackingObj) {
    // you can send data to omniture by calling s.t(myObj), but this will also send any data previously set on s
    // also, there might already exist code that sends events later on in the page, that expects data to sent, that was already set on s
    // the caller of this method might want to send only a specific ominture prop, and so we need to clear s. But,
    // we should restore it back just in case

    var sBackup = {};
    $.extend(sBackup, s);

    GS.tracking.clearSvariable(s);

    s.t(trackingObj);

    s = sBackup;
};

/**
 * send additional tracking data with some of the default props and evar set in s_code ignored, so they won't be duplicated -
 * this is done by setting usePlugins to false.
 * @param trackingObj
 */
GS.tracking.sendNewTrackingData= function(trackingObj) {

    var sBackup = {};
    $.extend(sBackup, s);

    GS.tracking.clearSvariable(sBackup);
    sBackup.usePlugins = false;
    sBackup.t(trackingObj);
};

GS.tracking.profile = GS.tracking.profile || (function(){
    var omnitureProfileNavElementName;

    var setOmnitureProfileNavElementName = function(elementName) {
        omnitureProfileNavElementName = elementName;
    };

    var getOmnitureProfileNavElementName = function() {
        return omnitureProfileNavElementName;
    }

    return {
        setOmnitureProfileNavElementName: setOmnitureProfileNavElementName,
        getOmnitureProfileNavElementName: getOmnitureProfileNavElementName
    };
})();

GS.tracking.data.updateProps = GS.tracking.data.updateProps || (function(){
    var setProps = function(tabName, props) {
        for (var key in props) {
            if(props.hasOwnProperty(key)) {
                GS.tracking.data[tabName].props[key] =  props[key];
            }
        }
    };

    return {
        setProps: setProps
    };
})();