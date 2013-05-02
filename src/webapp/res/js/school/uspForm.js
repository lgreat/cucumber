var GS = GS || {};
GS.form = GS.form || {};
GS.form.UspForm = function () {

    this.saveForm = function (form) {
        var masterDeferred = new jQuery.Deferred();
        if (!GS.form.uspForm.doValidations()) {
            alert('show error');
            return masterDeferred.reject().promise();
        }

        var data = form.serializeArray();

        jQuery.ajax({type:'POST', url:document.location, data:data}
        ).fail(function () {
                masterDeferred.reject();
                alert("Sorry! There was an unexpected error saving your form. Please wait a minute and try again.");
            }).done(function () {
            });
    };

    this.doValidations = function () {
        return true;
    };

};

GS.form.uspForm = new GS.form.UspForm();

jQuery(function () {
    var form = jQuery('#js_uspForm');

    form.on('click', '.js_submit', function () {
        GS.form.uspForm.saveForm(form);
        return false;
    });
});