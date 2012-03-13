GS.history5Enabled = false && typeof(window.History) !== 'undefined' && window.History.enabled === true;

GS.form = GS.form || {};
// make the visibility of a dom element(s) depend on the value of a form field
// for checkboxes, input value should be a boolean.
// TODO: need better name for this function?
// TODO: migrate elsewhere
// TODO: how to maintain state when event handler executes on multiple elements, to reduce number of iterations necessary
// to be able to correctly show/hide the "selectorOfElementToControl"
GS.form.controlVisibilityOfElementWithCheckbox = function(selectorOfElementToControl, masterFieldSelector, value, options) {
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

/*
 * function to control the visibility of form fields.
 * @param selectorOfElementToControl - field to show hide
 * @param masterFieldSelector - controlling field that shows or hides.
 * @param values - value or comma separated values of the controlling field that should show the element*/
GS.form.controlVisibilityOfElementWithRadio = function(selectorOfElementToControl, masterFieldSelector, values) {

    $(masterFieldSelector).on('change', function() {
        var objectsToCheck = jQuery(masterFieldSelector);
        var acceptableValues = new Array();
        acceptableValues = values.split(",");
        var match = false;

        objectsToCheck.each(function() {
            var thisObject = jQuery(this);
            if (jQuery.inArray(thisObject.val(), acceptableValues) >= 0 && thisObject.prop('checked')) {
                match = true;
                //Return false is used to break the each loop.
                return false;
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
    //http://pietschsoft.com/post/2010/11/16/HTML5-Day-3-Detecting-HTML5-Support-via-JavaScript.aspx

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

// patch jQuery's val method so that it will return empty string as value if value is equal to placeholder
$.fn.valIncludingPlaceholder = $.fn.val;
$.fn.val = function (value) {
    var field = $(this);
    if (typeof value == 'undefined') {
        var realVal = field.valIncludingPlaceholder();
        return realVal === field.attr("placeholder") ? "" : realVal;
    }

    return field.valIncludingPlaceholder(value);
};
$.fn.valEqualsPlaceholder = function() {
    var field = $(this);
    return field.valIncludingPlaceholder() === field.attr("placeholder");
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
        var ghostText = jQueryObject.attr('placeholder');
        if (eventData.type === 'focus') {
            if (jQueryObject.valEqualsPlaceholder()) {
                jQueryObject.val('');
                jQueryObject.removeClass('placeholder');
            }
        } else if (eventData.type === 'blur') {
            if (jQueryObject.val().length === 0) {
                jQueryObject.val(ghostText);
                jQueryObject.addClass('placeholder');
            }
        }
    });
    // set up initial state of items on page
    ghostTextableInputs.blur();
};
GS.form.clearGhostTextOnInputs = function(containerSelector) {
    $(containerSelector + ' ' + GS.form.GHOST_TEXTABLE_INPUT_SELECTOR).each(function() {
       var jQueryObject = $(this);
        if (jQueryObject.valEqualsPlaceholder()) {
            jQueryObject.val('');
        }
    });
};
// returns array of input names where their value is equal to the placeholder ghost text
GS.form.findInputsWithGhostTextAsValue = function(containerSelector) {
    var inputsWithGhostText = [];
    $(containerSelector + ' ' + GS.form.GHOST_TEXTABLE_INPUT_SELECTOR).each(function() {
        var jQueryObject = $(this);
        if (jQueryObject.valEqualsPlaceholder()) {
            inputsWithGhostText.push(jQueryObject.attr('name'));
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
// iterates through array of objects generated by jQuery.serializeArray, and clears their value if they are not visible
GS.form.handleHiddenElements = function(arrayOfObjects, containerSelector) {
    if (containerSelector === undefined) {
        containerSelector = '';
    }
    var hiddenInputs = jQuery(containerSelector).find(":input").not(":visible").not(":button").not(":submit");
    for (var i = 0; i < hiddenInputs.length; i++) {
        for (var j = 0; j < arrayOfObjects.length; j++) {
            if (arrayOfObjects[j].name === hiddenInputs[i].name) {
                arrayOfObjects[j].value = "";
            }
        }
    }
    return arrayOfObjects;
};

GS.form.removeElementsWithClass = function(arrayOfElements, containerSelector, className) {
    if (containerSelector === undefined) {
        containerSelector = '';
    }
    var forExclusion = jQuery(containerSelector).find("." + className);

    for (var i = 0; i < forExclusion.length; i++) {
        for (var j = 0; j < arrayOfElements.length; j++) {
            if (arrayOfElements[j].name === forExclusion[i].name && arrayOfElements[j].value == forExclusion[i].value) {
                arrayOfElements[j].value = "";
            }
        }
    }
    return arrayOfElements;
};


GS.validation = GS.validation || {};
GS.validation.ERROR_SUFFIX = "__error";

GS.validation.styleInputAsErrored = function(selectorOrObject) {
    if (typeof selectorOrObject === 'string') {
        selectorOrObject = $(selectorOrObject);
    }
    selectorOrObject.addClass('warning');
    var jQueryObj = GS.validation.findErrorForField(selectorOrObject);
    GS.validation.showErrors(jQueryObj);
};

GS.validation.styleInputAsPassed = function(selectorOrObject) {
    if (typeof selectorOrObject === 'string') {
        selectorOrObject = $(selectorOrObject);
    }
    selectorOrObject.removeClass('warning');
    var jQueryObj = GS.validation.findErrorForField(selectorOrObject);
    GS.validation.hideErrors(jQueryObj);
};

GS.validation.findErrorForField = function(selectorOrObject) {
    if (typeof selectorOrObject === 'string') {
        selectorOrObject = $(selectorOrObject);
    }
    var field = selectorOrObject;
    var errorSelector = ".error";

    // first look for the error element among siblings
    var errorObj = field.siblings().filter(errorSelector);

    // TODO: okay if length is more than 1? Allow there to be multiple errors per field?
    
    // if we haven't found the error element, look at aunts and uncles
    if (errorObj.length === 0) {
        errorObj = field.parent().parent().find(errorSelector);
    }

    // next, try to find the error by naming convention.
    if (errorObj.length === 0) {
        errorObj = $(field.attr('id') + GS.validation.ERROR_SUFFIX);
    }

    if (errorObj.length === 1) {
        return errorObj;
    }
};

GS.validation.showErrors = function(jQueryObj) {
    if (typeof jQueryObj === 'string') {
        jQueryObj = $(jQueryObj);
    }

    if (!jQueryObj instanceof jQuery) {
        return;
    }

    if (jQueryObj !== undefined && jQueryObj !== null && jQueryObj.length !== undefined && jQueryObj.length > 0) {
        jQueryObj.each(function() {
            $(this).show();
        })
    }
};

GS.validation.hideErrors = function(jQueryObj) {
    if (typeof jQueryObj === 'string') {
        jQueryObj = $(jQueryObj);
    }

    if (!jQueryObj instanceof jQuery) {
        return;
    }

    if (jQueryObj !== undefined && jQueryObj !== null && jQueryObj.length !== undefined && jQueryObj.length > 0) {
        jQueryObj.each(function() {
            $(this).hide();
        })
    }
};

/*GS.validation.setupValidationHandlers = function() {
    $('.js-val-phone').on('change blur', function() {
        GS.validation.validateAndStylePhone($(this));
    });

    $('.js-val-ph1, .js-val-ph2, .js-val-ph3').on('change blur', function() {
        GS.validation.validateAndStylePhoneParts($(this));
    });
};*/

GS.validation.validateAndStylePhone = function(jQueryObject) {
    if (typeof jQueryObject === 'string') {
        jQueryObject = $(jQueryObject);
    }

    var valid = true;

    if (!jQueryObject instanceof jQuery) {
        return true;
    }

    var phone = jQueryObject.val();

    if (phone !== undefined && phone.length > 0) {
        valid = GS.validation.TEN_DIGIT_PHONE_PATTERN.test(phone);
    }

    if (!valid) {
        GS.validation.styleInputAsErrored(jQueryObject);
    } else {
        GS.validation.styleInputAsPassed(jQueryObject);
    }

    return valid;
};

GS.validation.validateAndStylePhoneParts = function(jQueryObject) {
    if (typeof jQueryObject === 'string') {
        jQueryObject = $(jQueryObject);
    }

    if (!jQueryObject instanceof jQuery) {
        return true;
    }

    var siblingsAndSelf = jQueryObject.parent().children().filter('input');

    var valid = GS.validation.validatePhoneParts(jQueryObject);

    if (!valid) {
        siblingsAndSelf.each(function() {
            GS.validation.styleInputAsErrored($(this));
        });
    } else {
        siblingsAndSelf.each(function() {
            GS.validation.styleInputAsPassed($(this));
        });
    }

    return valid;
};

GS.validation.validatePhoneParts = function(jQueryObject) {
    if (typeof jQueryObject === 'string') {
        jQueryObject = $(jQueryObject);
    }

    if (!jQueryObject instanceof jQuery) {
        return true;
    }

    // TODO: make multi-part field validation more generic
    var siblingsAndSelf = jQueryObject.parent().children().filter('input');
    var valid = true;

    var phonePart1 = siblingsAndSelf.filter('.js-val-ph1').val();
    var phonePart2 = siblingsAndSelf.filter('.js-val-ph2').val();
    var phonePart3 = siblingsAndSelf.filter('.js-val-ph3').val();

    var compositePhone = "";
    if (phonePart1 !== undefined && phonePart1 !== null) {
        compositePhone += phonePart1.toString();
    }
    if (phonePart2 !== undefined && phonePart2 !== null) {
        compositePhone += phonePart2.toString();
    }
    if (phonePart3 !== undefined && phonePart3 !== null) {
        compositePhone += phonePart3.toString();
    }

    // validate integer and length 10, don't validate required
    if (compositePhone.length > 0) {
        valid = GS.validation.TEN_DIGIT_PHONE_PATTERN.test(compositePhone);
    }

    return valid;
};

GS.validation.validateRequired = function(fieldSelector, errorSelector) {
    var isValid = true;
    jQuery(errorSelector).hide();
    jQuery(fieldSelector).filter("input[type=text]").removeClass("warning");
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
        } else if (fieldType === 'text') {
            // require each one to have text
            formFields.each(function() {
                var field = jQuery(this);
                var fieldVal = jQuery.trim(field.val());
                if (fieldVal === "" || field.valEqualsPlaceholder()) {
                    isValid = false;
                    return false;
                }
            });
        } else if (formFields.is('select')) {
            if (formFields.val() === '') {
                isValid = false;
            }
        }
        if (!isValid) {
            jQuery(errorSelector).show();
            jQuery(fieldSelector).filter("input[type=text]").addClass("warning");
        }
    }
    return isValid;
};


/**
 * Used for radio buttons.If there is at least one radio button checked then validate required.
 */
GS.validation.validateRequiredIfChecked = function(fieldSelector, fieldCheckedSelector, errorSelector) {
    jQuery(errorSelector).hide();
    jQuery(fieldSelector).filter("input[type=text]").removeClass("warning");
    var checkedFields = jQuery(fieldCheckedSelector);
    if (checkedFields.filter(':checked').size() == 0) {
        return true;
    }
    return GS.validation.validateRequired(fieldSelector, errorSelector);
};

/**
 * Used for input boxes.If there is text in the input box then validate required.
 */
GS.validation.validateRequiredIfNotEmpty = function(fieldSelector, fieldInputSelector, errorSelector) {
    jQuery(errorSelector).hide();
    var inputFields = jQuery(fieldInputSelector);
    var checkRequired = false;
    inputFields.each(function() {
        var field = jQuery(this);
        var fieldVal = field === undefined ? '' : jQuery.trim(field.val());
        if (fieldVal !== "" && !field.valEqualsPlaceholder()) {
            checkRequired = true;
            //Return false is used to break out of the each loop.
            return false;
        }
    });
    if (checkRequired) {
        return GS.validation.validateRequired(fieldSelector, errorSelector);
    }
    return true;
};

// Enforces non-negative
// TODO: add option to enforce non-zero?
GS.validation.validateInteger = function(fieldSelector, errorSelector) {
    var isValid = true;
    jQuery(fieldSelector).filter("input[type=text]").removeClass("warning");
    jQuery(errorSelector).hide();
    var formFields = jQuery(fieldSelector).filter(':visible'); // only validate visible fields
    if (formFields !== undefined && formFields.size() > 0) {
        var fieldType = formFields.attr('type');

        if (fieldType == 'text') {
            // require each one to be numeric
            formFields.each(function() {
                var fieldVal = jQuery.trim(jQuery(this).val());
                if (fieldVal == '' || GS.validation.POSITIVE_INTEGER_PATTERN.test(fieldVal)) {
                    return true;
                } else {
                    isValid = false;
                    return false;
                }
            });
        }

        if (!isValid) {
            jQuery(errorSelector).show();
            jQuery(fieldSelector).filter("input[type=text]").addClass("warning");
        }
    }
    return isValid;
};

GS.validation.TEN_DIGIT_PHONE_PATTERN = /^\d{10}$/;
GS.validation.POSITIVE_INTEGER_PATTERN = /^\d+$/;
// taken from UrlValidator (and modified to proper JS syntax) in apache commons, which references RFC2396
GS.validation.URL_PATTERN = /^(([^:/?#]+):)?(\/\/([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?/;
GS.validation.validateUrl = function(url) {
    return GS.validation.URL_PATTERN.test(url);
};

GS.validation.EMAIL_PATTERN = /^[_a-zA-Z0-9+-]+(\.[_a-zA-Z0-9+-]+)*@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*(\.[a-zA-Z]{2,6})$/;
// GS.validation.EMAIL_PATTERN = /\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}\b/;
GS.validation.LEGAL_ASCII_PATTERN = /^[\000-\\177]+$/;

// very loose match, allows false positives
GS.validation.validateEmailFormat = function(email) {
    return GS.validation.EMAIL_PATTERN.test(email);
};

GS.validation.validateEmail = function(fieldSelector, errorSelector, required) {
    var pattern = /^(.+)@(.+)$/;
    var isValid = true;
    jQuery(fieldSelector).filter("input[type=text]").removeClass("warning");
    jQuery(errorSelector).hide();
    var formFields = jQuery(fieldSelector).filter(':visible');
    if (formFields !== undefined && formFields.size() > 0) {
        var fieldType = formFields.attr('type');

        if (fieldType == 'text') {
            // require each one to be numeric
            formFields.each(function() {
               var fieldVal = jQuery.trim(jQuery(this).val());
                if (required === true) {
                    isValid = fieldVal.length > 0 && GS.validation.validateEmailFormat(fieldVal);
                } else {
                    isValid = fieldVal.length === 0 || GS.validation.validateEmailFormat(fieldVal);
                }
            });
        }

        if (!isValid) {
            jQuery(errorSelector).show();
            jQuery(fieldSelector).filter("input[type=text]").addClass("warning");
        }
    }
    return isValid;
};

GS.validation.validateAllOrNone = function(fieldSelectors, errorSelector) {
    var fields = $(fieldSelectors).filter(':visible');
    var error = $(errorSelector);
    var valid = true;

    error.hide();
    fields.removeClass('warning');

    var all = true;
    var none = true;

    fields.each(function() {
        var field = $(this);
        var hasValue = field.val() !== undefined && field.val().length > 0;
        all = all && hasValue;
        none = none && !hasValue;
    });

    valid = (all || none);

    if (!valid) {
        error.show();
        fields.addClass('warning');
    }

    return valid;
};

GS.validation.validateSumPercentages = function(fieldSelector, errorSelector) {
    var isValid = true;
    var maxPercentage = 100;
    jQuery(fieldSelector).filter("input[type=text]").removeClass("warning");
    jQuery(errorSelector).hide();
    var formFields = jQuery(fieldSelector).filter(':visible'); // only validate visible fields
    if (formFields !== undefined && formFields.size() > 0) {
        var fieldType = formFields.attr('type');

        if (fieldType == 'text') {
            var total = 0;
            // require each one to be numeric
            formFields.each(function() {
                var fieldVal = parseInt(jQuery.trim(jQuery(this).val()), 10);
                if (fieldVal > 0) {
                    total += fieldVal;
                }
            });

            isValid = total <= maxPercentage;
        }

        if (!isValid) {
            jQuery(errorSelector).show();
            jQuery(fieldSelector).filter("input[type=text]").addClass("warning");
        }
    }
    return isValid;
};

// TODO: figure out what to do with this type of validation method
GS.validation.validateSelectIfTextboxValueEntered = function(selectBoxSelector, textBoxSelector, errorSelector, textBoxValid) {
    var selectBox = $(selectBoxSelector).filter(':visible');
    var textBox = $(textBoxSelector).filter(':visible');
    var error = $(errorSelector);
    var valid = true;
    var nothingSelectedValue = "";
    error.hide();
    selectBox.removeClass('warning');

    valid = !(textBoxValid && textBox.val() !== undefined && textBox.val().length > 0
        && selectBox.val() === nothingSelectedValue);

    if (!valid) {
        error.show();
        selectBox.addClass('warning');
    }

    return valid;
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
            // remove values for hidden elements so they get cleared on the back end
            data = GS.form.handleHiddenElements(data, '#espFormPage-' + GS.espForm.currentPage);
            // remove elements that are marked for exclusion
            data = GS.form.removeElementsWithClass(data, '#espFormPage-' + GS.espForm.currentPage, 'js_exclude');
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
                    if (data.formStarted !== undefined && data.formStarted && clickCapture) {
                        clickCapture.capture("events", "event60;");
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
        if (GS.espForm.school) {
            window.location = '/official-school-profile/dashboard/?schoolId=' + GS.espForm.school.id + '&state=' + GS.espForm.school.state;
        } else {
            window.location = '/official-school-profile/dashboard/';
        }
    };
    var sendToSignInPage = function() {
        window.location = '/official-school-profile/signin.page';
    };
    var sendToPageNumber = function(pageNum) {
        var myParams = GS.util.getUrlVars();
        if (GS.history5Enabled) {
            if (typeof GS.pollingPhotoViewer !== 'undefined') {
                if (pageNum === 6) {
                    GS.pollingPhotoViewer.turnPollingOn();
                } else {
                    GS.pollingPhotoViewer.turnPollingOff();
                }
            }
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
            subCookie.setObjectProperty("site_pref", "showHover", "confirmEspSave", 3);
            sendToLandingPage();
        });
    };
    var getVisibleFormInputNames = function(formElem) {
        var form = $(formElem);
        var allVisibleFormFields = form.find(":input").not(":button").not(":submit");
        var formNames = {};
        allVisibleFormFields.each(function() {
            if (this.name != '') {
                formNames[this.name] = 1;
            }
        });
        var formNameArr = new Array();
        for (var formName in formNames) {
            if (formNames.hasOwnProperty(formName) && formNames[formName] === 1) {
                formNameArr.push(formName);
            }
        }
        return formNameArr.join(',');
    };

    var validateGradeLevels = function() {
        jQuery('#form_grade_levels_error').hide();
        var form = jQuery('#espFormPage-' + GS.espForm.currentPage);
        // only validate if current page contains grade levels
        var gradeLevels = form.find('[name=grade_levels]');
        if (gradeLevels.size() > 0) {
            var checkedLevels = gradeLevels.filter(':checked');
            var numChecked = checkedLevels.size();
            if (numChecked == 0) {
                jQuery('#form_grade_levels_error').show();
                return false;
            } else if (numChecked == 1) {
                if (checkedLevels.filter('#form_grade_levels__pk').size() == 1) {
                    jQuery('#form_grade_levels_error').show();
                    return false;
                }
            } else if (numChecked == 2) {
                if (checkedLevels.filter('#form_grade_levels__pk, #form_grade_levels__kg').size() == 2) {
                    jQuery('#form_grade_levels_error').show();
                    return false;
                }
            }
        }
        return true;
    };


    var doValidations = function() {
        var validations = new Array();
        // PAGE 1
        jQuery('#form_student_enrollment_error').hide();
        jQuery('#form_student_enrollment_number_error').hide();
        validations.push(GS.validation.validateRequired('#form_student_enrollment', '#form_student_enrollment_error')
            && GS.validation.validateInteger('#form_student_enrollment', '#form_student_enrollment_number_error'));
        validations.push(validateGradeLevels());
        // END PAGE 1


        // PAGE 3
        var isValidApplicationsReceived = GS.validation.validateInteger('#form_applications_received', '#form_applications_received_error');
        validations.push(isValidApplicationsReceived);
        validations.push(GS.validation.validateSelectIfTextboxValueEntered('#form_applications_received_year', '#form_applications_received', '#form_applications_received_year_error', isValidApplicationsReceived));
        var isValidStudentsAccepted = GS.validation.validateInteger('#form_students_accepted', '#form_students_accepted_error');
        validations.push(isValidStudentsAccepted);
        validations.push(GS.validation.validateSelectIfTextboxValueEntered('#form_students_accepted_year', '#form_students_accepted', '#form_students_accepted_year_error', isValidStudentsAccepted));
        validations.push(GS.validation.validateAllOrNone('#form_tuition_low, #form_tuition_high, #form_tuition_year', '#form_tuition_error'));
        validations.push(GS.validation.validateInteger('#form_application_fee_amount', '#form_application_fee_amount_numeric_error'));
        // END PAGE 3

        // PAGE 4
//        var isValidForeignLanguageOther = GS.validation.validateRequiredIfChecked
//            ('#js_form_foreign_language_other', '.js_otherField_js_form_foreign_language_other', '#js_form_foreign_language_error');
        validations.push(GS.validation.validateRequiredIfChecked
            ('[name=special_ed_programs]', '[name=special_ed_programs_exists]', '#js_form_special_ed_programs_error'));
//        validations.push(GS.validation.validateRequiredIfChecked
//            ('[name=schedule]', '[name=schedule_exists]', '#js_form_schedule_error'));
//        var isValidExtraLearningResources = GS.validation.validateRequiredIfChecked
//            ('#js_form_extra_learning_resources_other', '.js_otherField_js_form_extra_learning_resources_other', '#js_form_extra_learning_resources_error');
//        var isValidStaffLanguages = GS.validation.validateRequiredIfChecked
//            ('#js_form_staff_languages_other', '.js_otherField_js_form_staff_languages_other', '#js_form_staff_languages_error');
//        var isValidCollegePrep = GS.validation.validateRequiredIfChecked
//            ('#js_form_college_prep_other', '.js_otherField_js_form_college_prep_other', '#js_form_college_prep_error');
        validations.push(GS.validation.validateRequiredIfNotEmpty('[name=post_graduation_year]','.js_form_post_graduation_year','#js_form_post_graduation_year_error'));
        validations.push(GS.validation.validateInteger('#js_form_post_graduation_2yr','#js_form_post_graduation_2yr_error'));
        validations.push(GS.validation.validateInteger('#js_form_post_graduation_4yr','#js_form_post_graduation_4yr_error'));
        validations.push(GS.validation.validateInteger('#js_form_post_graduation_military','#js_form_post_graduation_military_error'));
        validations.push(GS.validation.validateInteger('#js_form_post_graduation_vocational','#js_form_post_graduation_vocational_error'));
        validations.push(GS.validation.validateInteger('#js_form_post_graduation_workforce','#js_form_post_graduation_workforce_error'));
        validations.push(GS.validation.validateSumPercentages('#js_form_post_graduation_2yr, ' +
                '#js_form_post_graduation_4yr, ' +
                '#js_form_post_graduation_military, ' +
                '#js_form_post_graduation_vocational, ' +
                '#js_form_post_graduation_workforce', '#js_form_post_graduation_percentages_error')
        );
//        var isValidSkillsTraining = GS.validation.validateRequiredIfChecked
//            ('#js_form_skills_training_other', '.js_otherField_js_form_skills_training_other', '#js_form_skills_training_error');

//        var isValidForeignLanguageOther = GS.validation.validateRequiredIfChecked('#js_form_foreign_language_other', '#form_foreign_language__other', '#js_form_foreign_language_other_error');
//        var isValidExtraLearningResourcesOther = GS.validation.validateRequiredIfChecked('#js_form_extra_learning_resources_other', '#form_extra_learning_resources__other', '#js_form_extra_learning_resources_other_error');
//        var isValidStaffLanguagesOther = GS.validation.validateRequiredIfChecked('#js_form_staff_languages_other', '#form_staff_languages__other', '#js_form_staff_languages_other_error');
//        var isValidCollegePrepOther = GS.validation.validateRequiredIfChecked('#js_form_college_prep_other','#form_college_prep__other', '#js_form_college_prep_other_error');
//        var isValidSkillsTrainingOther = GS.validation.validateRequiredIfChecked('#js_form_skills_training_other', '#form_skills_training__other', '#js_form_skills_training_other_error');



        // END PAGE 4

        // PAGE 7
        validations.push(GS.validation.validateEmail('#form_administrator_email', '#form_administrator_email_error', false));

        validations.push(GS.validation.validateRequired('#form_physical_address_street', '#form_physical_address_street_error'));

        validations.push(GS.validation.validateAndStylePhoneParts('#form_school_phone_area_code'));
        validations.push(GS.validation.validateAndStylePhoneParts('#form_school_fax_area_code'));
        validations.push(GS.validation.validateRequiredIfChecked('#form_contact_method_phone', '#form_contact_method__phone', '#form_contact_method_phone_error')
            && GS.validation.validateAndStylePhone('#form_contact_method_phone'));
        validations.push(GS.validation.validateRequiredIfChecked('#form_contact_method_email', '#form_contact_method__email', '#form_contact_method_email_error')
            && GS.validation.validateEmail('#form_contact_method_email', '#form_contact_method_email_error'));
        validations.push(GS.validation.validateRequiredIfChecked('#form_contact_method_other', '#form_contact_method__other', '#form_contact_method_other_error'));
        // END PAGE 7

        // PAGE 8
        validations.push(GS.validation.validateInteger('#js_form_census_ell_esl','#js_form_census_ell_esl_number_error'));
        validations.push(GS.validation.validateInteger('#js_form_census_forpl','#js_form_census_forpl_number_error'));
        validations.push(GS.validation.validateInteger('#js_form_census_special_ed','#js_form_census_special_ed_number_error'));
        // END PAGE 8

        for (var arrayIndex in validations) {
            if (validations[arrayIndex] === false) {
                return false;
            }
        }
        return true;
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
        formWrapper.on('click', '.js_saveButton, .js_doneButton', function() {
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
        $('#js_espFormNav').on('click', 'a.js_saveForm', function() {
            var targetUrl = this.href;
            saveForm()
                .done(function() {
                    document.location = targetUrl;
                });
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
            if (field.val().length > 0 && !field.valEqualsPlaceholder()) {
                var otherField = formWrapper.find('.js_otherField_' + this.id);
                otherField.prop('checked', true);
            }
        });
        // any field linked to an 'other' text field that has no value or value == 'other' will be detached from
        // the back end
        jQuery(formWrapper).find('.js_otherField').each(function() {
            var otherField = formWrapper.find('.js_otherField_' + this.id);
            if (otherField.size() == 1 && (otherField.val().length == 0 || otherField.val() == 'other')
                && !otherField.hasClass('js_include')) {
                otherField.addClass('js_exclude');
                $(this).addClass('js_setDefaultMarker'); // since it is detached from back end, we need to set state via JS
            }
        });
        jQuery(formWrapper).find('.js_otherField').filter('.js_setDefaultMarker').change(); // trigger right away to set default state

        GS.form.controlVisibilityOfElementWithRadio('#form_age_pk_start_group', '[name=early_childhood_programs]', 'yes');
        GS.form.controlVisibilityOfElementWithCheckbox('#form_before_after_care_before_group','#form_before_after_care_before', true);
        GS.form.controlVisibilityOfElementWithCheckbox('#form_before_after_care_after_group','#form_before_after_care_after', true);
        GS.form.controlVisibilityOfElementWithRadio('#js_form_immersion_language_group','[name=immersion]', 'yes');

        GS.form.controlVisibilityOfElementWithRadio('#js_ell_languages','[name=ell_level]', 'moderate,intensive');
        GS.form.controlVisibilityOfElementWithRadio('#js_partnerships','[name=partnerships]', 'yes');
        GS.form.controlVisibilityOfElementWithRadio('#js_schedule','[name=schedule_exists]', 'yes');
        GS.form.controlVisibilityOfElementWithRadio('#js_special_ed_programs','[name=special_ed_programs_exists]', 'yes');

        GS.form.findAndApplyGhostTextSwitching('#espFormPage-' + GS.espForm.currentPage);

        // validate all visible fields when any input textbox value is changed
        formWrapper.on('change', 'input, select', function() {
            doValidations();
        });
        
        // page 3 specific
        GS.form.controlVisibilityOfElementWithRadio('#js_sctn_admissions_header, #js_sctn_admissions_contact_school, #sctn_admissions_url, #sctn_application_deadline, #sctn_applications_received, #sctn_students_accepted, #sctn_application_fee','[name=application_process]', 'yes');
        $('#form_applications_received').on('keyup', function(){
        	$('#form_applications_received_year').toggle($(this).val().length > 0)
        });
        $('#form_applications_received').keyup();
        $('#form_students_accepted').on('keyup', function(){
        	$('#form_students_accepted_year').toggle($(this).val().length > 0)
        });
        $('#form_students_accepted').keyup();

        GS.form.controlVisibilityOfElementWithRadio('#js_financial_aid_group', '[name=financial_aid]', 'yes');
        GS.form.controlVisibilityOfElementWithRadio('#js_application_fee_group', '[name=application_fee]', 'yes');
        GS.form.controlVisibilityOfElementWithRadio('#sctn_tuition_range', '[name=application_process]', 'yes');
        GS.form.controlVisibilityOfElementWithRadio('#sctn_students_vouchers', '[name=application_process]', 'yes');
        GS.form.controlVisibilityOfElementWithRadio('#sctn_financial_aid', '[name=application_process]', 'yes');
        // END page 3 specific


        // page 6
        $('#form_anything_else').on('keyup keydown blur', function() {
            var maxLength = 500;
            var textarea = $(this);
            if (textarea.val().length > maxLength) {
                textarea.val(textarea.val().substring(0,maxLength));
                alert('Only 500 characters are allowed.');
            }
            var currentLength = textarea.val().length;
            var characterCountElement = textarea.parent().find('.js-charactersLeft');
            if (currentLength === maxLength-1) {
                characterCountElement.html("1 character remaining");
            } else {
                characterCountElement.html(parseInt(maxLength - currentLength).toString() + " characters remaining");
            }
        });

        // page 7
        $('#form_facebook_url_none_none').on('change', function() {
            var noFacebookUrl = $(this).prop('checked');
            $('#form_facebook_url').prop('disabled', noFacebookUrl);
        });

        $('#form_school_url_none_none').on('change', function() {
            var noSchoolUrl = $(this).prop('checked');
            $('#form_school_url').prop('disabled', noSchoolUrl);
        });
    });
})();