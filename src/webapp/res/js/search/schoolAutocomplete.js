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
       }
    });

    searchStateSelect.blur(function() {
        searchBox.flushCache();
    });
};