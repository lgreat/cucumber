GS.history5Enabled = typeof(window.History) !== 'undefined' && window.History.enabled === true;

GS.validation = GS.validation || {};
GS.validation.validateRequired = function(fieldSelector, errorSelector) {
    var isValid = true;
    jQuery(errorSelector).hide();
    var formFields = jQuery(fieldSelector);
    if (formFields.filter(':visible').size() == 0) {
        return true; // only validate visible fields
    }
    if (formFields !== undefined && formFields.size() > 0) {
        var fieldType = formFields.attr('type');

        if (fieldType === 'checkbox') {
            // require at least one to be checked
            var numChecked = formFields.filter(':checked').size();
            if (numChecked == 0) {
                isValid = false;
            }
        } else if (fieldType == 'text') {
            // require each one to have text
            formFields.each(function() {
                var fieldVal = jQuery.trim(jQuery(this).val());
                if (fieldVal == "") {
                    isValid = false;
                    return false;
                }
            });
        }

        if (!isValid) {
            jQuery(errorSelector).show();
        }
    }
    return isValid;
};

GS.validation.validateInteger = function(fieldSelector, errorSelector) {
    var isValid = true;
    jQuery(errorSelector).hide();
    var formFields = jQuery(fieldSelector);
    if (formFields.filter(':visible').size() == 0) {
        return true; // only validate visible fields
    }
    if (formFields !== undefined && formFields.size() > 0) {
        var fieldType = formFields.attr('type');

        if (fieldType == 'text') {
            // require each one to be numeric
            formFields.each(function() {
                var fieldVal = parseInt(jQuery.trim(jQuery(this).val()), 10);
                if (!jQuery.isNumeric(fieldVal)) {
                    isValid = false;
                    return false; // breaks out of loop
                }
            });
        }

        if (!isValid) {
            jQuery(errorSelector).show();
        }
    }
    return isValid;
};

GS.util = GS.util || {};
GS.util.getUrlVars = function(url) {
    var myUrl = url || window.location.href;
    var vars = {};
    myUrl.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
};
GS.util.log = function(msg) {
    if (typeof(console) !== 'undefined') {
        console.log(msg);
    }
};

new (function() {
    var saveForm = function() {
        if (!doValidations()) {
            return new jQuery.Deferred().reject().promise();
        }
        var form = jQuery('#espFormPage-' + GS.espForm.currentPage);
        var data = form.serializeArray();
        return jQuery.ajax({type: 'POST', url: document.location, data: data}
        ).fail(function() {
            alert("Error");
        }).done(function(data) {
            if (data.percentComplete !== undefined) {
                GS.util.log("Updating percent complete for page " + GS.espForm.currentPage + " to " + parseInt(data.percentComplete));
                GS.espForm.percentComplete[GS.espForm.currentPage] = parseInt(data.percentComplete);
            }
        });
    };
    var sendToLandingPage = function() {
        window.location = '/school/esp/dashboard.page';
    };
    var sendToPageNumber = function(pageNum) {
        var myParams = GS.util.getUrlVars();
        if (GS.history5Enabled) {
            // use HTML 5 history API to rewrite the current URL to represent the new state.
            window.History.pushState({page:pageNum}, document.title, '?page=' + pageNum + '&schoolId=' + myParams.schoolId + '&state=' + myParams.state);
        } else {
            window.location = '/school/esp/form.page?page=' + pageNum + '&schoolId=' + myParams.schoolId + '&state=' + myParams.state;
        }
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
//            sendToLandingPage();
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
        var isValidAcademicFocus = GS.validation.validateRequired('[name=academic_focus]', '#academic_focus_error');
        var isValidStudentEnrollment = GS.validation.validateRequired('#form_student_enrollment', '#form_student_enrollment_error')
            && GS.validation.validateInteger('#form_student_enrollment', '#form_student_enrollment_error');
        return  isValidAcademicFocus && isValidStudentEnrollment;
    };

    if (GS.history5Enabled) {
        History.Adapter.bind(window, 'statechange', function() {
            var state = History.getState();
            var pageNum = parseInt(GS.util.getUrlVars(state.url).page,10) || 1;
            GS.util.log("Loading state for page " + pageNum);
            GS.espForm.currentPage = pageNum;
            $('#js_pageContainer').find('.js_pageWrapper').hide();
            $('#js_pageWrapper-' + pageNum).show();
            $('#js_espPercentComplete').html(GS.espForm.percentComplete[pageNum]);
        });
    }
    jQuery(function() {
        var formWrapper = $('#js_pageContainer');
        formWrapper.on('click', '.js_saveButton', function() {
            saveAndFinish();
            return false;
        });
        formWrapper.on('click', '.js_nextPageButton', function() {
            saveAndNextPage();
            return false;
        });
        formWrapper.on('click', '.js_prevPageButton', function() {
            saveAndPreviousPage();
            return false;
        });
        formWrapper.on('submit', 'form', function() {
            saveForm();
            return false;
        });
        formWrapper.on('change', '.js_otherField', function() {
            var field = $(this);
            var otherFieldName = field.attr('id').substring(3);
            var otherField = formWrapper.find('[name=' + otherFieldName + ']');
            if (!field.prop('checked')) { // TODO: Might not work for radios?
                otherField.val(''); // TODO: Only works for text inputs
            }
        });
    });
})();
