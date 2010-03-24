if (GSType == undefined) {
    var GSType = {};
}

if (GSType.hover == undefined) {
    GSType.hover = {};
}

Function.prototype.gs_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};

//HoverDialog requires the ID of the element to display as a hover dialog
GSType.hover.HoverDialog = function(id) {
    this.hoverId = id;
    this.show = function() {
        jQuery('#' + this.hoverId).dialog('open');
    };
    this.hide = function() {
        jQuery('#' + this.hoverId).dialog('close');
    };
    //template dialog to display based on variable width
    this.dialogByWidth = function (width) {
        jQuery('#' + this.hoverId).dialog({
            bgiframe: true,
            modal: true,
            draggable: false,
            autoOpen: false,
            resizable: false,
            width: width
        });
        jQuery('.' + this.hoverId + '_showHover').click(this.show.gs_bind(this));
        jQuery('.' + this.hoverId + '_hideHover').click(this.hide.gs_bind(this));
    };
};

//EditEmailValidated Hover
GSType.hover.EditEmailValidated = function() {
    this.loadDialog = function () {
        this.dialogByWidth(600);
    }
};
GSType.hover.EditEmailValidated.prototype = new GSType.hover.HoverDialog('valNewEmailDone');

//EmailValidated hover
GSType.hover.EmailValidated = function() {
    this.loadDialog = function () {
        this.dialogByWidth(600);
    }
};
GSType.hover.EmailValidated.prototype = new GSType.hover.HoverDialog('regDone');

//ForgotPasswordHover hover
GSType.hover.ForgotPasswordHover = function() {
    this.loadDialog = function() {
        this.dialogByWidth(600);
    };
    this.addMessage = function(text) {
        jQuery('#hover_forgotPassword .messages').append('<p>' + text + '</p>');
    };
    this.clearMessages = function() {
        jQuery('#hover_forgotPassword .messages').replaceWith('<div class="messages"><!-- not empty --></div>')
    };
    this.showJoin = function() {
        GSType.hover.forgotPassword.hide();
        GSType.hover.joinHover.show();
    };
};
GSType.hover.ForgotPasswordHover.prototype = new GSType.hover.HoverDialog('hover_forgotPassword');

//Join hover
GSType.hover.JoinHover = function() {
    this.loadDialog = function() {
        this.dialogByWidth(680);
    }
};
GSType.hover.JoinHover.prototype = new GSType.hover.HoverDialog('joinHover');

//SignInHover hover
GSType.hover.SignInHover = function() {
    this.loadDialog = function() {
        this.dialogByWidth(680);
    };

    this.addMessage = function(text) {
        jQuery('#signInHover .messages').append('<p>' + text + '</p>');
    };
    this.clearMessages = function() {
        jQuery('#signInHover .messages').replaceWith('<div class="messages"><!-- not empty --></div>');
    }
};
GSType.hover.SignInHover.prototype = new GSType.hover.HoverDialog('signInHover');

//ValidateEditEmail Hover
GSType.hover.ValidateEditEmail = function() {
    this.loadDialog = function() {
        this.dialogByWidth(600);
    }
};
GSType.hover.ValidateEditEmail.prototype = new GSType.hover.HoverDialog('valEditEmail');

//ValidateEmailHover Hover
GSType.hover.ValidateEmailHover = function() {
    this.loadDialog = function() {
        this.dialogByWidth(400);
    }
};
GSType.hover.ValidateEmailHover.prototype = new GSType.hover.HoverDialog('valEmail');

//ValidateLinkExpired hover
GSType.hover.ValidateLinkExpired = function() {
    this.loadDialog = function() {
        this.dialogByWidth(400);
    }
};
GSType.hover.ValidateLinkExpired.prototype = new GSType.hover.HoverDialog('expVer');

//EmailNotValidated hover
GSType.hover.EmailNotValidated = function() {
    this.loadDialog = function() {
        this.dialogByWidth(600);
    }
};
GSType.hover.EmailNotValidated.prototype = new GSType.hover.HoverDialog('valNewEmail');

GSType.hover.forgotPassword = new GSType.hover.ForgotPasswordHover();
GSType.hover.emailValidated = new GSType.hover.EmailValidated();
GSType.hover.editEmailValidated = new GSType.hover.EditEmailValidated();
GSType.hover.emailNotValidated = new GSType.hover.EmailNotValidated();
GSType.hover.validateEmail = new GSType.hover.ValidateEmailHover();
GSType.hover.joinHover = new GSType.hover.JoinHover();
GSType.hover.signInHover = new GSType.hover.SignInHover();
GSType.hover.validateEditEmail = new GSType.hover.ValidateEditEmail();
GSType.hover.validateLinkExpired = new GSType.hover.ValidateLinkExpired();

function loginValidatorHandler(data) {
    var objCount = 0;
    for (_obj in data) objCount++;

    if (objCount > 0) {
        if (data.noSuchUser) {
            GSType.hover.signInHover.addMessage('<p class="error">' + data.noSuchUser + '</p>');
        }
        if (data.userNoPassword) {
            GSType.hover.signInHover.clearMessages();
            GSType.hover.signInHover.hide();
            //GSType.hover.joinHover.addMessage(data.userNoPassword);
            GSType.hover.joinHover.show();
        }
        if (data.userNotValidated) {
            GSType.hover.signInHover.clearMessages();
            GSType.hover.signInHover.hide();
            GSType.hover.emailNotValidated.show();
        }
        if (data.email) {
            GSType.hover.signInHover.addMessage('<p class="error">' + data.email + '</p>');
        }
        if (data.userDeactivated) {
            GSType.hover.signInHover.addMessage('<p class="error">' + data.userDeactivated + '</p>');
        }
        if (data.passwordMismatch) {
            GSType.hover.signInHover.addMessage('<p class="error">' + data.passwordMismatch + '</p>');
        }
    } else {
        var params = {
            email: jQuery('#semail').val(),
            password: jQuery('#spword').val()
        };

        jQuery('#signin').submit();

        GSType.hover.signInHover.hide();
    }
}

function checkValidationResponse(data) {
    GSType.hover.forgotPassword.clearMessages();

    if (data.errorMsg) {
        GSType.hover.forgotPassword.addMessage(data.errorMsg);
        return;
    }

    jQuery.post('/community/forgotPassword.page', jQuery('#hover_forgotPasswordForm').serialize());
    var email = jQuery('#fpemail').val();

    GSType.hover.signInHover.addMessage('An email has been sent to ' + email +
                                        ' with instructions for selecting a new password.');
    GSType.hover.signInHover.show();
    GSType.hover.forgotPassword.hide();
}

jQuery(function() {
    GSType.hover.editEmailValidated.loadDialog();
    GSType.hover.emailNotValidated.loadDialog();
    GSType.hover.emailValidated.loadDialog();
    GSType.hover.forgotPassword.loadDialog();
    GSType.hover.joinHover.loadDialog();
    GSType.hover.signInHover.loadDialog();
    GSType.hover.validateEditEmail.loadDialog();
    GSType.hover.validateEmail.loadDialog();
    GSType.hover.validateLinkExpired.loadDialog();

    jQuery('#hover_forgotPasswordSubmit').click(function() {
        jQuery.post('/community/forgotPasswordValidator.page',
                jQuery('#hover_forgotPasswordForm').serialize(),
                checkValidationResponse,
                'json');

        return false;
    });

    jQuery('#hover_forgotPassword').bind('dialogclose', function() {
        GSType.hover.forgotPassword.clearMessages();
    });


    jQuery('.signInHoverLink').click(function() {
        GSType.hover.signInHover.show();
    });

    jQuery('#signinBtn').click(function() {
        GSType.hover.signInHover.clearMessages();

        var params = {
            email: jQuery('#semail').val(),
            password: jQuery('#spword').val()
        };

        jQuery.post('/community/registration/popup/loginValidationAjax.page', params, loginValidatorHandler, "json");

        return false;
    });

    jQuery('#signInHover').bind('dialogclose', function() {
        GSType.hover.signInHover.clearMessages();
    });

    jQuery('#signin').attr("action", "/community/loginOrRegister.page?redirect=" + window.location.href);

    
});
