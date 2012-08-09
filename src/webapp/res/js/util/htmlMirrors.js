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
        if(($elementToCopyFrom.length == 0 || $elementToCopyFrom.html() == '') && $dataRecipient.attr('id') === 'js_totalResultsCountReturn') {
            $dataRecipient.html('0');
            $('#js-noResultsPopup').show();
        }
        else {
            var count = $elementToCopyFrom.html();
            if(parseInt(count) === 1) {
                $('#js-onlyOne').show();
            }
            else if(parseInt(count) > 1) {
                $('#js-moreThanOne').show();
            }
            $dataRecipient.html(count);
        }
    };

    var updateAll = function() {
        $('#js-noResultsPopup').hide();
        $('#js-onlyOne').hide();
        $('#js-moreThanOne').hide();
        $("#js-spinny-search").hide();
        jQuery("#js_totalResultsCountReturn").show();
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