/**
 * Created with IntelliJ IDEA.
 * User: mseltzer
 * Date: 4/30/13
 * Time: 9:57 AM
 * To change this template use File | Settings | File Templates.
 */
/*
    ****** How to call ******
    BrowserDetect.browser == 'Explorer';
    BrowserDetect.version <= 9;
*/

var GS = GS || {};
GS.lib = GS.lib || {};

GS.lib.BrowserDetect =
{
    init: function ()
    {
        this.browser = this.searchString(this.dataBrowser) || "Other";
        this.version = this.searchVersion(navigator.userAgent) || this.searchVersion(navigator.appVersion) || "Unknown";
    },

    searchString: function (data)
    {
        for (var i=0 ; i < data.length ; i++)
        {
            var dataString = data[i].string;
            this.versionSearchString = data[i].subString;

            if (dataString.indexOf(data[i].subString) != -1)
            {
                return data[i].identity;
            }
        }
    },

    searchVersion: function (dataString)
    {
        var index = dataString.indexOf(this.versionSearchString);
        if (index == -1) return;
        return parseFloat(dataString.substring(index+this.versionSearchString.length+1));
    },

    dataBrowser:
        [
            { string: navigator.userAgent, subString: "Chrome",  identity: "Chrome" },
            { string: navigator.userAgent, subString: "MSIE",    identity: "Explorer" },
            { string: navigator.userAgent, subString: "Firefox", identity: "Firefox" },
            { string: navigator.userAgent, subString: "Safari",  identity: "Safari" },
            { string: navigator.userAgent, subString: "Opera",   identity: "Opera" }
        ]

};
GS.lib.BrowserDetect.init();