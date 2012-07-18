// used when the inner HTML of one element needs to match the inner HTML of some other element

var GS = GS || {};
GS.util = GS.util || {};
GS.util.htmlMirrors = GS.util.htmlMirrors || (function() {
    var recipientDataAttribute = 'gs-html-mirror';
    //var triggerUpdateAllEvent = 'gs-update-html-mirrors';


    var getRecipientSelector = function(mirrorId) {
        return '[data-' + recipientDataAttribute + '=' + mirrorId + ']';
    };

    var init = function() {
        /*$(function() {
            setupUpdateAllTrigger();
        });*/
    };

    /*var setupUpdateAllTrigger = function() {
        $('body').bind(triggerUpdateAllEvent, function() {
           updateAll();
        });
    };*/

    var updateOneMirror = function(mirrorIdOrJq) {
        var $dataRecipient;
        if (mirrorIdOrJq instanceof $) {
            $dataRecipient = mirrorIdOrJq;
        } else {
            $dataRecipient = $(getRecipientSelector(mirrorIdOrJq));
        }

        var $elementToCopyFrom = $('#' + $dataRecipient.data(recipientDataAttribute));
        if(($elementToCopyFrom.length == 0 || $elementToCopyFrom.html() == '') && $dataRecipient.attr('id') === 'totalResultsText') {
            $dataRecipient.html('0');
            $('#js-noResultsPopup').show();
        }
        else {
            $dataRecipient.html($elementToCopyFrom.html());
        }
    };

    var updateAll = function() {
        $('#js-noResultsPopup').hide();
        $("#js-spinny-search").hide();
        jQuery("#totalResultsText").show();
        $('[data-' + recipientDataAttribute + ']').each(function() {
            updateOneMirror($(this));
        });
    };

    return {
        init:init,
        updateOneMirror:updateOneMirror,
        updateAll:updateAll,
        recipientDataAttribute:recipientDataAttribute
    };

})();
GS.util.htmlMirrors.init();