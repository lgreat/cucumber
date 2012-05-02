define(function(){
    // Ideas for improvement:
    // To validate one field against multiple validation functions, allow validation type to take a comma separated list of validation types

    // To control one error with multiple fields, allow data-validation to contain a comma separated list of validation names, and
    // Show the error when all the validations fail. (Unknown how to do this... might require some changes to logic)

    var failures;

    var init = function() {
        failures = [];
    };

    var dataAttributes = {
        validationName:'validation',
        validationType:'validation-type',
        validationError:'validation-error-message'
    };

    var validations = {
        required:'required'
    };

    var createMessageSelector = function(parentSelector, validationName, validationType) {
        return parentSelector + ' [data-' + dataAttributes.validationName + '=' + validationName + '][data-' + dataAttributes.validationError + '=' + validationType + ']';
    };

    var createDataAttributeSelector = function(parentSelector, dataAttribute, optionalValue) {
        if (optionalValue === undefined) {
            return parentSelector + ' [data-' + dataAttribute + ']';
        } else {
            return parentSelector + ' [data-' + dataAttribute + '=' + optionalValue + ']';
        }
    };

    var validationFunctions = {
        required: function($element) {
            var value = $element.val();
            var placeholder = $element.attr('placeholder');
            if (value === undefined || value === null || value.length === 0 || (placeholder !== undefined && value === placeholder)) {
                return false;
            }
            return true;
        }
    };

    var attachValidationHandler = function(parentSelector, on, specificSelector) {
        $(parentSelector).on(on, specificSelector, function() {
            validateOne($(this), parentSelector);
        });
    };

    var attachValidationHandlers = function(parentSelector) {
        $(parentSelector + ' select[data-' + dataAttributes.validationType + ']').on('change', function() {
            validateOne($(this), parentSelector);
        });
    };

    var validateOne = function($element, parentSelector) {
        var validationType = $element.data(dataAttributes.validationType);
        var validationName = $element.data(dataAttributes.validationName);
        // TODO: if multiple validations are needed, use csv and split on comma
        var $errorMessage = $(createMessageSelector(parentSelector, validationName, validationType));
        var valid = validationFunctions[validationType]($element);
        $errorMessage.toggle(!valid);
    };

    var validateAll = function(parentSelector) {
        $(createDataAttributeSelector(parentSelector, dataAttributes.validationType)).each(function() {
            validateOne($(this), parentSelector);
        });
    };

    return {
        init:init,
        validateOne:validateOne,
        validateAll:validateAll,
        attachValidationHandlers:attachValidationHandlers
    };

});