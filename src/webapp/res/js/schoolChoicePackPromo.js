// required to avoid "$" collisions with Prototype.js
jQuery.noConflict();

var $j = jQuery;

$j(function() {
    $j("input#send").click(function() {
        var levelChecked = false;
        $j('.ck').each (function () {
            if (this.checked) levelChecked = true;
        });

        if (levelChecked) {
            var emailIsValid = false;
            $j.get("/util/isValidEmail.page", {email : $j('input#cemail').val()},
                    function (data) {
                        if (data == 'true') {
                            $j.getJSON("/promo/schoolChoicePackPromo.page",
                                    function(datax){
                                        // todo: process json
                                        alert ("deb 1");
                                    });
                            emailIsValid = true;
                        }
                    }, "text");
        }
        alert ("levelChecked: " + levelChecked);
        return false;
    });
});

