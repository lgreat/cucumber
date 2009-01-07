$(function() {
    $("input#send").click(function() {
        var levelChecked = false;
        $('.ck').each (function () {
            if (this.checked) levelChecked = true;
        });

        if (levelChecked) {
            var emailIsValid = false;
            $.get("/util/isValidEmail.page", {email : $('input#cemail').val()},
                    function (data) {
                        if (data == 'true') {
                            $.getJSON("/promo/schoolChoicePackPromo.page",
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

