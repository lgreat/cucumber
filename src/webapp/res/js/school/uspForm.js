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
        // TODO: pass correct values
        data.push({name:"email", value:"rramachandran+usp+test2@greatschools.org"},{name:"firstName", value:"Ramprasad"},
            {name:"password", value:"testing"},{name:"confirmPassword", value:"testing"},{name:"terms", value:"true"});

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
function uspSpriteCheckBoxes(containerLayer, fieldToSet, checkedValue, uncheckedValue){
    container = $("."+containerLayer);
    checkOn  = container.find(".js-checkBoxSpriteOn");
    checkOff = container.find(".js-checkBoxSpriteOff");
    checkBoxField =  $("#"+fieldToSet);
    checkOff.on("click", function(){
        $(this).hide();
        $(this).siblings().show();
        checkBoxField.val(checkedValue);
    });
    checkOn.on("click", function(){
        $(this).hide();
        $(this).siblings().show();
        checkBoxField.val(uncheckedValue);
    });
}

jQuery(function () {
    GSType.hover.modalUspRegistration.show();
    uspSpriteCheckBoxes("js-needText", "form_needText", 1, 0);

    var form = jQuery('#js_uspForm');
    form.on('click', '.js_submit', function () {
        GS.form.uspForm.saveForm(form);
        return false;
    });
});