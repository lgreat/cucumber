var GS = GS || {};
GS.search = GS.search || {};
GS.search.searchBySchoolNameForm = GS.search.searchBySchoolNameForm || (function() {
    var SEARCH_PAGE_PATH = '/search/search.page';
    var FORM_SELECTOR = '#jq-findByNameForm';
    var DEFAULT_SEARCH_FIELD_TEXT = 'School name';
    var SEARCH_FIELD_SELECTOR = '#js-findByNameBox';

    var init = function() {
        var $form = jQuery(FORM_SELECTOR);
        $form.on('click', '#js-submit', function(){
            if(DEFAULT_SEARCH_FIELD_TEXT === $form.find(SEARCH_FIELD_SELECTOR).val()) {
                return true;
            }
            return submitForm($form);
        });
    };

    var submitForm = function($form) {
        var searchString = $form.find(SEARCH_FIELD_SELECTOR).val();
        var collectionId = $form.find('#jq-collectionId').val();
        var state = $form.find('#jq-state').val();

        var queryStringData = GS.uri.Uri.getQueryData();

        queryStringData.q = encodeURIComponent(searchString);
        queryStringData.collectionId = encodeURIComponent(collectionId);
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