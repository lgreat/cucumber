var GS = GS || {};

GS.attachSchoolAutocomplete = function(domId) {
    var searchBox = jQuery('#' + domId);
    var searchStateSelect = jQuery('#stateSelector');
    var url = "/search/schoolAutocomplete.page";
    searchBox.autocomplete(url, {
        extraParams: {
            state: function() {
                return searchStateSelect.val();
            }
        },
        minChars: 3,
        selectFirst: false
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