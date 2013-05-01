var saveForm = function(form) {
    var masterDeferred = new jQuery.Deferred();
    if(!doValidations()) {
        alert('show error');
        return masterDeferred.reject().promise();
    }

    var data = form.serializeArray();

    jQuery.ajax({type: 'POST', url: document.location, data: data}
    ).fail(function() {
            masterDeferred.reject();
            alert("Sorry! There was an unexpected error saving your form. Please wait a minute and try again.");
        }).done(function() {});
};

var doValidations = function() {
    return true;
};

jQuery(function() {
    var form = jQuery('#js_uspForm');
    form.on('click', '.js_submit', function() {
        saveForm(form);
        return false;
    });
});