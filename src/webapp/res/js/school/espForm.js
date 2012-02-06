GS.history5Enabled = false && typeof(window.History) !== 'undefined' && window.History.enabled === true;

GS.form = GS.form || {};
// make the visibility of a dom element(s) depend on the value of a form field
// for checkboxes, input value should be a boolean.
// TODO: need better name for this function?
// TODO: migrate elsewhere
// TODO: how to maintain state when event handler executes on multiple elements, to reduce number of iterations necessary
// to be able to correctly show/hide the "selectorOfElementToControl"
GS.form.controlVisibilityOfElement = function(selectorOfElementToControl, masterFieldSelector, value, options) {
    // match any and match all only compatable with checkboxes
    var matchAny = (options !== undefined && options.matchAny === true);
    var matchAll = (options !== undefined && options.matchAll === true);

    $(masterFieldSelector).on('change', function() {
        var objectsToCheck;
        var match = false; // whether or not field value matches provided value
        if (matchAll) {
            match = true;
        }

        // if we're going to inspect multiple fields, set objectsToCheck to all of elements the selector matches
        // otherwise just set it to the element that triggered this event
        if (matchAny || matchAll) {
            objectsToCheck = jQuery(masterFieldSelector);
        } else {
            objectsToCheck = jQuery(this);
        }

        objectsToCheck.each(function() {
            var thisObject = jQuery(this);
            var itemMatch;
            if (thisObject.is(':checkbox')) {
                itemMatch = (value === thisObject.prop('checked'));
            } else if (thisObject.is(':radio')) {
                itemMatch = (thisObject.val() === value && thisObject.prop('checked'));
            } else {
                itemMatch = (thisObject.val() === value);
            }

            // use options to determine whether to OR or AND the checkbox match results together
            if (matchAny) {
                match = match || itemMatch;
            } else if (matchAll) {
                match = match && itemMatch;
            } else {
                match = itemMatch;
            }
        });

        if (match) {
            jQuery(selectorOfElementToControl).show();
        } else {
            jQuery(selectorOfElementToControl).hide();
        }
    });
    $(masterFieldSelector).change(); // trigger right away to set default state
};

// TODO: move to util/detect.js
GS.util = GS.util || {};
GS.util.isAttributeSupported = function(tagName, attrName) {
    http://pietschsoft.com/post/2010/11/16/HTML5-Day-3-Detecting-HTML5-Support-via-JavaScript.aspx

    var val = false;
    // Create element
    var input = document.createElement(tagName);
    // Check if attribute (attrName)
    // attribute exists
    if (attrName in input) {
        val = true;
    }
    // Delete "input" variable to
    // clear up its resources
    delete input;
    // Return detected value
    return val;
};


GS.form.GHOST_TEXTABLE_INPUT_SELECTOR = "input[placeholder]";
// to use, set the "placeholder" attribute on your html input form elements to desired ghost text and run this on load
GS.form.findAndApplyGhostTextSwitching = function(containerSelector) {
    // bail if browser supports placeholder ghost text by default (in html5)
    if (GS.util.isAttributeSupported('input','placeholder')) {
        return true;
    }

    var ghostTextableInputs = $(containerSelector + ' ' + GS.form.GHOST_TEXTABLE_INPUT_SELECTOR);

    ghostTextableInputs.on('focus blur', function(eventData) {
        var jQueryObject = $(this);
        var ghostText = jQueryObject.prop('placeholder');
        if (eventData.type === 'focus') {
            if (jQueryObject.val() === ghostText) {
                jQueryObject.val('');
            }
        } else if (eventData.type === 'blur') {
            if (jQueryObject.val().length === 0) {
                jQueryObject.val(ghostText);
            }
        }
    });
    // set up initial state of items on page
    ghostTextableInputs.each(function() {
        var jQueryObject = $(this);
        var ghostText = jQueryObject.prop('placeholder');
        if (jQueryObject.val().length === 0) {
            jQueryObject.val(ghostText);
        }
    });
};
GS.form.clearGhostTextOnInputs = function(containerSelector) {
    $(containerSelector + ' ' + GS.form.GHOST_TEXTABLE_INPUT_SELECTOR).each(function() {
       var jQueryObject = $(this);
        if (jQueryObject.val() === jQueryObject.prop('placeholder')) {
            jQueryObject.val('');
        }
    });
};
// returns array of input names where their value is equal to the placeholder ghost text
GS.form.findInputsWithGhostTextAsValue = function(containerSelector) {
    var inputsWithGhostText = [];
    $(containerSelector + ' ' + GS.form.GHOST_TEXTABLE_INPUT_SELECTOR).each(function() {
        var jQueryObject = $(this);
        if (jQueryObject.val() === jQueryObject.prop('placeholder')) {
            inputsWithGhostText.push(jQueryObject.prop('name'));
        }
    });
    return inputsWithGhostText;
};
// iterates through array of objects generated by jQuery.serializeArray, and clears their value if it's the ghost text
GS.form.handleInputsWithGhostTextAsValue = function(arrayOfObjects, containerSelector) {
    // bail if browser supports placeholder ghost text by default (in html5)
    if (GS.util.isAttributeSupported('input','placeholder')) {
        return arrayOfObjects;
    }

    if (containerSelector === undefined) {
        containerSelector = '';
    }
    var inputsWithGhostText = GS.form.findInputsWithGhostTextAsValue(containerSelector);
    for (var i = 0; i < inputsWithGhostText.length; i++) {
        for (var j = 0; j < arrayOfObjects.length; j++) {
            if (arrayOfObjects[j].name === inputsWithGhostText[i]) {
                //arrayOfObjects.splice(j,1);
                arrayOfObjects[j].value = "";
            }
        }
    }
    return arrayOfObjects;
};


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

// Enforces non-negative
// TODO: add option to enforce non-zero?
GS.validation.validateInteger = function(fieldSelector, errorSelector) {
    var isValid = true;
    jQuery(errorSelector).hide();
    var formFields = jQuery(fieldSelector).filter(':visible'); // only validate visible fields
    if (formFields !== undefined && formFields.size() > 0) {
        var fieldType = formFields.attr('type');

        if (fieldType == 'text') {
            // require each one to be numeric
            formFields.each(function() {
                var fieldVal = parseInt(jQuery.trim(jQuery(this).val()), 10);
                if (isNaN(fieldVal) || fieldVal < 0) {
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
        var masterDeferred = new jQuery.Deferred();
        try {
            if (!doValidations()) {
                return masterDeferred.reject().promise();
            }
            var form = jQuery('#espFormPage-' + GS.espForm.currentPage);
            var data = form.serializeArray();
            data = GS.form.handleInputsWithGhostTextAsValue(data, '#espFormPage-' + GS.espForm.currentPage);
            data.push({name:"_visibleKeys", value:getVisibleFormInputNames(form)});
            jQuery.ajax({type: 'POST', url: document.location, data: data}
            ).fail(function() {
                    masterDeferred.reject();
                    alert("Sorry! There was an unexpected error saving your form. Please wait a minute and try again.");
                }).done(function(data) {
                    if (data.error !== undefined) {
                        masterDeferred.reject();
                        var genericErrorMessage = data.error;
                        if (genericErrorMessage == 'noUser') {
                            sendToSignInPage();
                        } else if (genericErrorMessage == 'noSchool') {
                            sendToLandingPage();
                        } else {
                            alert("There was an error saving the form. Please double-check all the fields and try again.");
                        }
                        return;
                    } else if (data.errors !== undefined) {
                        masterDeferred.reject();
                        var validationErrorMessage = "The following field(s) had validation errors: \n\n";
                        for (var errorKey in data) {
                            if (data.hasOwnProperty(errorKey)) {
                                if (errorKey != 'errors' && errorKey != 'error') {
                                    validationErrorMessage += errorKey + ": " + data[errorKey] + '\n';
                                }
                            }
                        }
                        alert(validationErrorMessage);
                        return;
                    }
                    if (data.percentComplete !== undefined) {
                        GS.util.log("Updating percent complete for page " + GS.espForm.currentPage + " to " + parseInt(data.percentComplete));
                        GS.espForm.percentComplete[GS.espForm.currentPage] = parseInt(data.percentComplete);
                    }
                    masterDeferred.resolve();
                });
        } catch (e) {
            alert("Sorry! There was an unexpected error saving your form. Please double-check all the fields and try again.");
            GS.util.log(e);
        }
        return masterDeferred.promise();
    };
    var sendToLandingPage = function() {
        window.location = '/official-school-profile/dashboard/';
    };
    var sendToSignInPage = function() {
        window.location = '/official-school-profile/signin.page';
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
            sendToLandingPage();
        });
    };
    var getVisibleFormInputNames = function(formElem) {
        var form = $(formElem);
        var allVisibleFormFields = form.find(":input").not(":button").not(":submit");
        var formNames = {};
        allVisibleFormFields.each(function() {
            formNames[this.name] = 1;
        });
        var formNameArr = new Array();
        for (var formName in formNames) {
            if (formNames.hasOwnProperty(formName) && formNames[formName] === 1) {
                formNameArr.push(formName);
            }
        }
        return formNameArr.join(',');
    };

    var doValidations = function() {
        var isValidAcademicFocus = GS.validation.validateRequired('[name=academic_focus]', '#academic_focus_error');
        var isValidStudentEnrollment = GS.validation.validateRequired('#form_student_enrollment', '#form_student_enrollment_error')
            && GS.validation.validateInteger('#form_student_enrollment', '#form_student_enrollment_error');

        var isValidClassSize = GS.validation.validateInteger('#form_average_class_size', '#form_average_class_size_error');


        return  isValidClassSize && isValidAcademicFocus && isValidStudentEnrollment;
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
//        $('#js_espFormNav').on('click', 'li', function() {
//            var elem = this;
//            saveForm().done(function() {
//                sendToPageNumber($('#js_espFormNav li').index(elem) + 1);
//            });
//            return false;
//        });
        // TODO: Remove following handler. Keep for now as it is useful for debugging to trigger saves w/o page change
        formWrapper.on('submit', 'form', function() {
            saveForm();
            return false;
        });
        // any text field with class js_otherField will cause another field with class "js_otherField_{this.id}"
        // to become checked when the text field is modified to contain text.
        formWrapper.on('change', '.js_otherField', function() {
            var field = $(this);
            if (field.val().length > 0) {
                var otherClassSelector = '.js_otherField_' + this.id;
                var otherField = formWrapper.find(otherClassSelector);
                otherField.prop('checked', true);
            }
        });

        /*$('#form_grade_levels_pk, #form_grade_levels_kg').on('change', function() {
            if ($('#form_grade_levels_pk').prop('checked') || $('#form_grade_levels_kg').prop('checked')) {
                $('#form_k3_offered_group, #form_k4_offered_group').show();
            } else {
                $('#form_k3_offered_group, #form_k4_offered_group').hide();
            }
        });
        $('#form_grade_levels_pk').change(); // trigger k3_offered and k3_offered update
        */
        GS.form.controlVisibilityOfElement('#form_k3_offered_group, #form_k4_offered_group', '#form_grade_levels_pk, #form_grade_levels_kg', true, {matchAny:true});
        GS.form.controlVisibilityOfElement('#form_school_type_affiliation_group', '[name=school_type]', 'private');
        GS.form.controlVisibilityOfElement('#form_age_pk_start_group', '#form_grade_levels_pk', true);
        GS.form.controlVisibilityOfElement('#form_before_after_care_before_group','#form_before_after_care', true);
        GS.form.controlVisibilityOfElement('#form_before_after_care_after_group','#form_before_after_care_after', true);

        GS.form.controlVisibilityOfElement('#js_form_immersion_language_group','[name=immersion]', 'yes');

        GS.form.findAndApplyGhostTextSwitching('#espFormPage-' + GS.espForm.currentPage);


    });
})();