jQuery('#srch2').click(function() {
    jQuery('#srch1').removeClass('active');
    jQuery(this).addClass('active');
});
jQuery('#srch1').click(function() {
    jQuery('#srch12').removeClass('active');
    jQuery(this).addClass('active');
});


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
        selectFirst: false,
        cacheLength: 150,
        matchSubset: true,
        max: 6,
        autoFill: false
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