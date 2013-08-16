var GS = GS || {};
GS.search = GS.search || {};
GS.search.searchBySchoolNameForm = GS.search.searchBySchoolNameForm || (function() {
    var SEARCH_PAGE_PATH = '/search/search.page';
    var FORM_SELECTOR = '#jq-findByNameForm';
    var DEFAULT_SEARCH_FIELD_TEXT = 'School name';
    var SEARCH_FIELD_SELECTOR = '#js-findByNameBox';

    var init = function() {
        var $form = jQuery(FORM_SELECTOR);
        $form.submit(function() {
            if(DEFAULT_SEARCH_FIELD_TEXT === $form.find(SEARCH_FIELD_SELECTOR).val()) {
                return true;
            }
            return submitForm($form);
        });
    };

    var submitForm = function($form) {
        var searchString = $form.find(SEARCH_FIELD_SELECTOR).val();
        var hub = $form.find('#jq-hub').val();
        var state = $form.find('#jq-state').val();

        var queryStringData = GS.uri.Uri.getQueryData();

        queryStringData.q = encodeURIComponent(searchString);
        queryStringData.hub = encodeURIComponent(hub);
        queryStringData.state = state;

        window.location.href = window.location.protocol + '//' + window.location.host +
            SEARCH_PAGE_PATH +
            GS.uri.Uri.getQueryStringFromObject(queryStringData);

        return false;
    };

    return {
        init: init
    };
})();

jQuery(function() {
    GS.search.searchBySchoolNameForm.init();
});