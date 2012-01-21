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

    var validateField = function(formField, error) {
        var isValid = true;
        if (formField != undefined) {
            var fieldType = formField.attr('type');

            if (fieldType == 'checkbox' && formField.length > 0) {
                var numChecked = $(formField).filter(':checked').size();
//                var firstElem = formElem[0];
//                alert($(firstElem).is(':checked'));
                if (numChecked <= 0) {
                    isValid = false;
                }
            } else if (fieldType == 'text') {
                formField = $.trim(formField.val());
                if (formField == "") {
                    isValid = false;
                }
            }

            if (!isValid) {
                $(error).show();
            }
        }
        return isValid;
    };


    var doValidations = function() {
        var isValidAcademicFocus = validateField($('[name=academic_focus]'), $('#academic_focus_error'));
        var isValidStudentEnrollment = validateField($('#form_student_enrollment'), $('#form_student_enrollment_error'));
        return  isValidAcademicFocus && isValidStudentEnrollment;
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
        formWrapper.find('.js_otherField').on('change', function() {
            var field = $(this);
            var otherFieldName = field.attr('id').substring(3);
            var otherField = formWrapper.find('[name=' + otherFieldName + ']');
            if (!field.prop('checked')) { // TODO: Might not work for radios?
                otherField.val(''); // TODO: Only works for text inputs
            }
        });
    });
})();
