var $j = jQuery;

$j(function() {

    var sitePreferences = subCookie.getObject("site_pref");
    var showSchoolChoicePackConfirm = "";

    if (sitePreferences != undefined && sitePreferences.showSchoolChoicePackConfirm != undefined) {
        showSchoolChoicePackConfirm = sitePreferences.showSchoolChoicePackConfirm;
    }

    if (showSchoolChoicePackConfirm == "true") {
        jQuery("#form_panel").hide();
        jQuery("#confirm_panel").show();
    }

    subCookie.deleteObjectProperty("site_pref", "showSchoolChoicePackConfirm");

    // clears the email field
    $j('#cemail').click(function () {
        if (this.value == 'Enter email address') {
            this.value = '';
        }
    });
    // show grade choices
    $j('#grdShow_SCPP').click(function() {
        if ($j('#grdShow_SCPP').hasClass('active')) {
            $j('#grdShow_SCPP').removeClass('active');
            $j('#moreGrades_SCPP').removeClass('show');
        } else {
            $j('#grdShow_SCPP').addClass('active');
            $j('#moreGrades_SCPP').addClass('show');
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

        jQuery('#scpp_form .emailError').hide();
        jQuery('#scpp_form .termsError').hide();
        jQuery('#scpp_form .gradeError').hide();

        var validationPassed = true;

        if (emailVal == "Enter email address" || !emailVal) {
            jQuery('#scpp_form .emailError').show();
            jQuery('#scpp_form .emailError .error').show();
            validationPassed = false;
        }

        if (!termsChecked) {
            jQuery('#scpp_form .termsError').show();
            jQuery('#scpp_form .termsError .error').show();
            validationPassed = false;
        }

        if (!cks.length > 0) {
            jQuery('#scpp_form .gradeError').show();
            jQuery('#scpp_form .gradeError .error').show();
            validationPassed = false;
        }

        if (!validationPassed) {
            return false;
        }

        $j.get("/util/isValidEmail.page", {email : emailVal},
                function (data) {
                    if (data == 'true') {
                        // GS-11425 Remove join requirement from Chooser Pack email
//                        if (GS.showChooserTipSheetHover(emailVal, window.location.href)) {
                            $j.post("/promo/schoolChoicePackPromo.page",
                            {email : emailVal, levels : cks.join(','), pageName : clickCapture.pageName, redirectForConfirm : window.location.href},
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
//                        }

                    } else {
                        jQuery('#scpp_form .emailError').show();
                        jQuery('#scpp_form .emailError .error').show();

                    }
                }, "text");


        return false;
    });


});
