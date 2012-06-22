/**
 * Created with IntelliJ IDEA.
 * User: mseltzer
 * Date: 6/19/12
 * Time: 1:56 PM
 * To change this template use File | Settings | File Templates.
 */
var SignInModal = (function($){
    var layerId = "signInHover";
    var email = "semail";
    var password = "spword";
    var onSubmitCallback = "";
    var formName = "signIn";
    var submitButton = "signinBtn";
    var showJoinFunction = "";

    return{
        hideModal : function ( ){
            ModalManager.hideModal({
                'layerId' : layerId
            });
        },
        showModal : function( email, redirect, showJoinFunction, onSubmitCallback ){
        if (onSubmitCallback) {
             this.onSubmitCallback = onSubmitCallback;
             } else {
             this.onSubmitCallback = null;
             }
             this.setEmail(email);
             this.setRedirect(redirect);
             if (showJoinFunction) {
             this.showJoinFunction = showJoinFunction;
             }
            ModalManager.showModal({
                'layerId' : layerId
            });

        },
        addMessage : function( message ){
            var message = '<p><span>\u00BB</span> ' + message + '</p>';
            modalManager.addMessage({
                'layerId' : layerId,
                'message' : message
            });
        },
        clearMessages : function( ){
            modalManager.clearMessages({
                'layerId' : layerId
            });
        },
        setEmail : function(email) {
            $('#'+layerId+' #semail').val(email);
        },
        setRedirect : function(redirect) {
            $('#'+layerId).find('.redirect_field').val(redirect);
        },
        loadOnExit : function(url) {
            var loadOnExitUrl = url;
            this.setRedirect(url);
            $('#fullPageOverlay').bind('onModalClose', function(event, containerId, paramLayerId) {
                if(layerId == paramLayerId){
                    console.log("inside onModalClose"+" : "+containerId+" : "+paramLayerId );
                    window.location = loadOnExitUrl;
                }
            });
        },
        validateFields : function() {
            $('#'+ layerId + ' .errors .error').hide();
            var params = {
                email: $('#'+email).val(),
                password: $('#'+password).val()
            };
            $.getJSON(GS.uri.Uri.getBaseHostname() + '/community/registration/popup/loginValidationAjax.page', params,
                this.loginValidatorHandler);

            return false;
        },
        showJoin : function() {
            if (this.loadOnExitUrl) {
                GSType.hover.joinHover.loadOnExit(this.loadOnExitUrl);
                this.cancelLoadOnExit();
            }
            if (this.onSubmitCallback) {
                GSType.hover.joinHover.onSubmitCallback = this.onSubmitCallback;
            }
            //GSType.hover.signInHover.hide();
            modalManager.hideModal({
                'layerId' : layerId
            });
            this.showJoinFunction();
            return false;
        },
        showForgotPassword : function() {
            if (this.loadOnExitUrl) {
                GSType.hover.forgotPassword.loadOnExit(this.loadOnExitUrl);
                this.cancelLoadOnExit();
            }
            //GSType.hover.signInHover.hide();
            modalManager.hideModal({
                'layerId' : layerId
            });
            GSType.hover.forgotPassword.show();
            return false;
        },
        loginValidatorHandler : function(data) {
            $('#'+ layerId +' .errors .error').hide();
            if (data.noSuchUser) {
                $('#'+layerId+' .errors .error').html(data.noSuchUser).show();
            } else if (data.userNoPassword) {
                this.clearMessages();
                //GSType.hover.signInHover.hide();
                modalManager.hideModal({
                    'layerId' : layerId
                });
                GSType.hover.joinHover.showJoinGlobalHeader();
                GSType.hover.joinHover.addMessage(data.userNoPassword);
            } else if (data.userNotValidated) {
                this.clearMessages();
                modalManager.hideModal({
                    'layerId' : layerId
                });
                GSType.hover.emailNotValidated.setEmail($('#'+email).val());
                GSType.hover.emailNotValidated.show();
            } else if (data.email) {
                $('#'+ layerId +' .errors .error').html(data.email).show();
            } else if (data.userDeactivated) {
                $('#'+ layerId +' .errors .error').html(data.userDeactivated).show();
            } else if (data.passwordMismatch) {
                $('#'+ layerId +' .errors .error').html(data.passwordMismatch).show();
            } else {
                this.cancelLoadOnExit();
                if (this.onSubmitCallback) {
                    this.onSubmitCallback($('#'+ email +'').val(), formName);
                } else {
                    $('#'+formName).submit();
                    modalManager.hideModal({
                        'layerId' : layerId
                    });
                }
            }
        }
    }
})(jQuery);