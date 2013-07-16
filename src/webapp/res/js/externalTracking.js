// requires omniture script

var pageTracking = {
    pageName: "",
    server: "",
    hierarchy: "",
    successEvents: "",
    eVars: {},
    props: {},

// http://stackoverflow.com/questions/15261644/how-to-pass-a-list-comma-separated-to-a-traffic-variable-in-omniture -
// the delimiter can be anything
    lists: {},

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
        for (var list in this.lists){
            s[list] = this.lists[list];
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
        this.lists = {};
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
        GS.tracking.data[key].lists = {};
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

    if (data.lists !== undefined) {
        var listData = data.lists;
        for (var l in listData) {
            if (listData.hasOwnProperty(l)) {
                if (listData[l] !== '') {
                    destination.lists[l] = listData[l];
                }
            }
        }
    }

    for (var k in data) {
        if (data.hasOwnProperty(k)) {
            var item = data[k];
            if (item !== '' && k !== 'props' && k !== 'lists') {
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
        GS.tracking.removeTabSpecificLists(key, data);
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

GS.tracking.tabSpecificListsMap = GS.tracking.tabSpecificListsMap || {};
GS.tracking.addTabSpecificLists = function(tabName, listKey) {
    if(GS.tracking.tabSpecificListsMap[tabName] === undefined) {
        GS.tracking.tabSpecificListsMap[tabName] = {};
        GS.tracking.tabSpecificListsMap[tabName].lists = [];
    }

    var listsMap = GS.tracking.tabSpecificListsMap[tabName].lists;

    if(listKey !== undefined) {
        listsMap.push(listKey);
    }
};

GS.tracking.removeTabSpecificLists = function(currentTab, data) {
    if (currentTab === undefined || data === undefined) {
        return;
    }
    for(var tab in GS.tracking.tabSpecificListsMap) {
        if(tab !== currentTab && GS.tracking.tabSpecificListsMap.hasOwnProperty(tab) && GS.tracking.tabSpecificListsMap[tab].lists !== undefined) {
            for(var i in GS.tracking.tabSpecificListsMap[tab].lists) {
                var list = GS.tracking.tabSpecificListsMap[tab].lists[i];
                if(list !== undefined) {
                    data.lists[list] = '';
                }
            }
        }
    }
};

/**
 * Based off of:
 * http://stackoverflow.com/questions/7692746/javascript-omniture-how-to-clear-all-properties-of-an-object-s-object
 */
GS.tracking.clearSvariable = function() {
    var maxPropsAndEvars = 75; // Omniture currently gives us a max of 75 props and evars
    var svarArr = ['pageName','channel','products','events','campaign','purchaseID','state','zip','server','linkName'];
    var maxLists = 3;
    var i;

    i = maxPropsAndEvars;
    while (i--) {
        s['prop'+i]='';
        s['eVar'+i]='';
        if(i <= 5) {
            s['hier'+i]='';
        }
    }

    i = svarArr.length;
    while (i--) {
        s[svarArr[i]]='';
    }

    i = maxLists;
    while(i--) {
        s['list'+i]='';
    }
};

GS.tracking.sendAndRestore= function(trackingObj) {
    // you can send data to omniture by calling s.t(myObj), but this will also send any data previously set on s
    // also, there might already exist code that sends events later on in the page, that expects data to sent, that was already set on s
    // the caller of this method might want to send only a specific ominture prop, and so we need to clear s. But,
    // we should restore it back just in case

    var sBackup = {};
    $.extend(sBackup, s);

    GS.tracking.clearSvariable();

    s.t(trackingObj);

    s = sBackup;
};