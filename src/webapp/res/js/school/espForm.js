new (function() {
    GS.util = GS.util || {};
    GS.util.getUrlVars = function() {
        var vars = {};
        window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
            vars[key] = value;
        });
        return vars;
    };

    var saveForm = function() {
        var form = jQuery('#espFormPage' + GS.espForm.currentPage);
        var data = form.serializeArray();
        var deferred = jQuery.ajax({type: 'POST', url: document.location, data: data}
        ).fail(function() {
                alert("Error");
            }
        );
        return deferred.promise();
    };
    var sendToLandingPage = function() {
        window.location = '/school/esp/dashboard.page';
    };
    var sendToPageNumber = function(pageNum) {
        var myParams = GS.util.getUrlVars();
        window.location = '/school/esp/form.page?page=' + pageNum + '&schoolId=' + myParams.schoolId + '&state=' + myParams.state;
    };
    var saveAndNextPage = function() {
        saveForm().done(function() {
            // fetch next page
            var nextPage = GS.espForm.currentPage + 1;
            if (nextPage > GS.espForm.maxPage) {
                sendToLandingPage();
            } else {
                sendToPageNumber(nextPage);
            }
        });
    };
    var saveAndPreviousPage = function() {
        saveForm().done(function() {
            // fetch previous page
            var previousPage = GS.espForm.currentPage - 1;
            if (previousPage < 1) {
                sendToLandingPage();
            } else {
                sendToPageNumber(previousPage);
            }
        });
    };
    var saveAndFinish = function() {
        saveForm().done(function() {
            sendToLandingPage();
        });
    };
    jQuery(function() {
        var formWrapper = $('#espFormWrapper');
        formWrapper.find('.js_saveButton').on('click', function() {
            saveAndFinish();
            return false;
        });
        formWrapper.find('.js_nextPageButton').on('click', function() {
            saveAndNextPage();
            return false;
        });
        formWrapper.find('.js_prevPageButton').on('click', function() {
            saveAndPreviousPage();
            return false;
        });
        formWrapper.find('form').on('submit', function() {
            saveForm();
            return false;
        });
    });
})();
