var GS = GS || {};

GS.attachSchoolAutocomplete = function(domId) {
    var searchBox = jQuery('#' + domId);
    var searchStateSelect = jQuery('#stateSelector');
    var url = window.location.protocol + '//' + window.location.host + "/search/schoolAutocomplete.page";
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