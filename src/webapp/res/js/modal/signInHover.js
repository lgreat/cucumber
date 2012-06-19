function signInModalManager(){
    var layerId = "signInHover";

    return
        hide : function(){
            modalManager.hideModal({
                'layerId' : layerId
            });,
        show : function(){
            modalManager.showModal({
                'layerId' : layerId
            });,
        addMessage: function( message ){
            var message = '<p><span>\u00BB</span> ' + message + '</p>';
            modalManager.addMessage({
               'layerId' : layerId,
               'message' : message
            });,
        },
        clearMessages : function( ){
            modalManager.clearMessages({
                'layerId' : layerId
            });
        }
}

//SignInHover hover
SignInHover = function() {
    var showJoinFunction = GSType.hover.joinHover.showJoinTrackGrade;
    var loadOnExitUrl = null;
    var onSubmitCallback = null;
    var pageName='Sign In Hover';
    var hier1='Hovers,Sign In,Sign In Hover';
    var layerId = "signInHover";
    this.addMessage = function(text) {
        jQuery('#signInHover .messages').append('<p><span>\u00BB</span> ' + text + '</p>');
    };
    this.clearMessages = function() {
        var signInHover = jQuery('#signInHover');
        signInHover.find('.messages').empty();
        signInHover.find('.errors .error').hide();
    };
    this.setEmail = function(email) {
        jQuery('#signInHover #semail').val(email);
    };
    this.setRedirect = function(redirect) {
        var signInHover = jQuery('#signInHover');
        signInHover.find('.redirect_field').val(redirect);
    };
    this.loadOnExit = function(url) {
        GSType.hover.signInHover.loadOnExitUrl = url;
        GSType.hover.signInHover.setRedirect(url);
        jQuery('#fullPageOverlay').bind('onModalClose', function(event, containerId, layerId) {
            if(layerId == "signInHover"){
                window.location = GSType.hover.signInHover.loadOnExitUrl;
                console.log("inside onModalClose"+" : "+containerId+" : "+layerId );
            }
        });
//        jQuery('#signInHover').bind('dialogclose', function() {
//            window.location = GSType.hover.signInHover.loadOnExitUrl;
//        });
    };
    this.cancelLoadOnExit = function() {
        GSType.hover.signInHover.loadOnExitUrl = null;
        jQuery('#signInHover').unbind('dialogclose');
    };
    this.validateFields = function() {
        jQuery('#signInHover .errors .error').hide();

        var params = {
            email: jQuery('#semail').val(),
            password: jQuery('#spword').val()
        };

        jQuery.getJSON(GS.uri.Uri.getBaseHostname() + '/community/registration/popup/loginValidationAjax.page', params,
                GSType.hover.signInHover.loginValidatorHandler);

        return false;
    };
    this.loginValidatorHandler = function(data) {
        jQuery('#signInHover .errors .error').hide();
        if (data.noSuchUser) {
            jQuery('#signInHover .errors .error').html(data.noSuchUser).show();
        } else if (data.userNoPassword) {
            GSType.hover.signInHover.clearMessages();
            //GSType.hover.signInHover.hide();
            modalManager.hideModal({
                'layerId' : 'signInHover'
            });
            GSType.hover.joinHover.showJoinGlobalHeader();
            GSType.hover.joinHover.addMessage(data.userNoPassword);
        } else if (data.userNotValidated) {
            GSType.hover.signInHover.clearMessages();
            modalManager.hideModal({
                'layerId' : 'signInHover'
            });
            //GSType.hover.signInHover.hide();
            GSType.hover.emailNotValidated.setEmail(jQuery('#semail').val());
            GSType.hover.emailNotValidated.show();
        } else if (data.email) {
            jQuery('#signInHover .errors .error').html(data.email).show();
        } else if (data.userDeactivated) {
            jQuery('#signInHover .errors .error').html(data.userDeactivated).show();
        } else if (data.passwordMismatch) {
            jQuery('#signInHover .errors .error').html(data.passwordMismatch).show();
        } else {
            GSType.hover.signInHover.cancelLoadOnExit();
            if (GSType.hover.signInHover.onSubmitCallback) {
                GSType.hover.signInHover.onSubmitCallback(jQuery('#semail').val(), "signin");
            } else {
                jQuery('#signin').submit();
                modalManager.hideModal({
                    'layerId' : 'signInHover'
                });
                //GSType.hover.signInHover.hide();
            }
        }
    };
    this.showHover = function(email, redirect, showJoinFunction, onSubmitCallback) {
        if (onSubmitCallback) {
            GSType.hover.signInHover.onSubmitCallback = onSubmitCallback;
        } else {
            GSType.hover.signInHover.onSubmitCallback = null;
        }
        GSType.hover.signInHover.setEmail(email);
        GSType.hover.signInHover.setRedirect(redirect);
        if (showJoinFunction) {
            GSType.hover.signInHover.showJoinFunction = showJoinFunction;
        }
        jQuery('#signinBtn').click(GSType.hover.signInHover.validateFields);
//        alert("Here");
        modalManager.showModal({
            'layerId' : 'signInHover'
        });
//        GSType.hover.signInHover.show();
        return false;
    };
    this.showJoin = function() {
        if (GSType.hover.signInHover.loadOnExitUrl) {
            GSType.hover.joinHover.loadOnExit(GSType.hover.signInHover.loadOnExitUrl);
            GSType.hover.signInHover.cancelLoadOnExit();
        }
        if (GSType.hover.signInHover.onSubmitCallback) {
            GSType.hover.joinHover.onSubmitCallback = GSType.hover.signInHover.onSubmitCallback;
        }
        //GSType.hover.signInHover.hide();
        modalManager.hideModal({
            'layerId' : 'signInHover'
        });
        GSType.hover.signInHover.showJoinFunction();
        return false;
    };
    this.showForgotPassword = function() {
        if (GSType.hover.signInHover.loadOnExitUrl) {
            GSType.hover.forgotPassword.loadOnExit(GSType.hover.signInHover.loadOnExitUrl);
            GSType.hover.signInHover.cancelLoadOnExit();
        }
        //GSType.hover.signInHover.hide();
        modalManager.hideModal({
            'layerId' : 'signInHover'
        });
        GSType.hover.forgotPassword.show();
        return false;
    }
};
GSType.hover.SignInHover.prototype = new GSType.hover.HoverDialog('signInHover',590);
