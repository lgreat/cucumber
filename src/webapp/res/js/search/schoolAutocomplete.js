/*
Requires: /uri/Uri.js
*/

var GS = GS || {};

GS.attachSchoolAutocomplete = function(domId) {
    var searchBox = jQuery('#' + domId);
    var searchStateSelect = jQuery('#stateSelector');
    var url = GS.uri.Uri.getBaseHostname() + "/search/schoolAutocomplete.page";
    searchBox.autocomplete(url, {
        extraParams: {
            state: function() {
                var rval = searchStateSelect.val();
                if (rval === '') {
                    return null;
                }
                return rval;
            }
        },
        extraParamsRequired:true,
        minChars: 3,
        selectFirst: false,
        cacheLength: 150,
        matchSubset: true,
        max: 6,
        autoFill: false,
        dataType: 'text'
    });

    searchStateSelect.blur(function() {
        searchBox.flushCache();
    });
};

GS.detachSchoolAutocomplete = function(domId) {
    var searchBox = jQuery('#' + domId);
    //jquery autocomplete plugin documentation sucks. .unautocomplete() and autocomplete("disable") don't work.
    searchBox.unbind(".autocomplete");
};