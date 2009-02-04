// required to avoid "$" collisions with Prototype.js
jQuery.noConflict();
var $j = jQuery;

$j(function() {

    // clears the email field
    $j('#cemail').click(function () {
        if (this.value == 'Enter Email Address') {
            this.value = '';
        }
    });

    // handles form validation and ajax processing
    $j("#scpp_form").submit(function() {
        var cks = new Array();
        $j('.ck').each (function () {
            if (this.checked) {
                cks.push(this.name);
            }
        });
        var emailVal = $j('input#cemail').val();
        var termsChecked = document.getElementById('privacy_terms_checkbox').checked;
        if (cks.length > 0) {
            if (emailVal) {
                $j.get("/util/isValidEmail.page", {email : emailVal},
                        function (data) {
                            if (data == 'true') {
                                if (termsChecked) {
                                    $j.post("/promo/schoolChoicePackPromo.page",
                                    {email : emailVal, levels : cks.join(','), pageName : clickCapture.pageName},
                                            function(datax){
                                                createCookie("MEMID", datax.memid);
                                                $j("#form_panel").hide();
                                                $j("#confirm_panel").show();
                                                omnitureEventNotifier.clear();
                                                omnitureEventNotifier.successEvents = datax.omnitureTracking.successEvents;
                                                omnitureEventNotifier.eVars = datax.omnitureTracking.eVars;
                                                omnitureEventNotifier.send();
                                            }, "json");
                                } else {
                                    alert ("Please accept the GreatSchools Privacy Policy and Terms of Use.");
                                }
                            } else {
                                alert ("Please enter a valid email address");
                            }
                        }, "text");
            } else {
                alert ("Please enter your email address");                
            }
        } else {
            if (emailVal) {
                alert ("Please select the grade level(s) of the school(s) you are choosing");
            } else {
                alert ("Please enter your email address and select the\ngrade level(s) of the school(s) you are choosing");
            }
        }
        return false;
    });
});

