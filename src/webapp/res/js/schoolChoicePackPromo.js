var $j = jQuery;

$j(function() {

    

    // clears the email field
    $j('#cemail').click(function () {
        if (this.value == 'Enter email address') {
            this.value = '';
        }
    });

    // handles form validation and ajax processing
    $j("#scpp_form").submit(function() {
        var cks = new Array();
        $j('.ck').each(function () {
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
                                    if (GS.showJoinHover(emailVal, window.location.href, GSType.hover.joinHover.showJoinChooserTipSheet)) {
                                        $j.post("/promo/schoolChoicePackPromo.page",
                                        {email : emailVal, levels : cks.join(','), pageName : clickCapture.pageName, redirectForConfirm : document.getElementById('redirectForConfirm').value},
                                                function(datax) {
                                                    // per GS-8301 don't set memid cookie here
                                                    //                                                if (datax.createMemberCookie == 'y') {
                                                    // must create cookie here even though it's in the controller already because
                                                    // this is an ajax call and we must make sure next page is aware of
                                                    // the MEMID. if only set in controller, the next page loaded may
                                                    // not see that cookie.
                                                    //                                                    createCookie("MEMID", datax.memid);
                                                    //                                                }
                                                    omnitureEventNotifier.clear();
                                                    omnitureEventNotifier.successEvents = datax.omnitureTracking.successEvents;
                                                    omnitureEventNotifier.eVars = datax.omnitureTracking.eVars;
                                                    omnitureEventNotifier.send();
                                                    if (datax.showRegistration == 'y') {
                                                        //storeHrefOpenHover(datax.redirectEncoded,'/community/registration/popup/sendToDestination.page',datax.emailEncoded);
                                                        //GSType.hover.joinHover.showJoinChooserTipSheet(emailVal);
                                                    } else {
                                                        $j("#form_panel").hide();
                                                        $j("#confirm_panel").show();
                                                    }

                                                }, "json");
                                    }

                                } else {
                                    alert("Please accept the GreatSchools Privacy Policy and Terms of Use.");
                                }
                            } else {
                                alert("Please enter a valid email address");
                            }
                        }, "text");
            } else {
                alert("Please enter your email address");
            }
        } else {
            if (emailVal) {
                alert("Please select the grade level(s) of the school(s) you are choosing");
            } else {
                alert("Please enter your email address and select the\ngrade level(s) of the school(s) you are choosing");
            }
        }
        return false;
    });


});

