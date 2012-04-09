define(['jquery'], function($) {
    var init = function() {
        $('.js-linkToTop').click(function() {
            $("html,body").animate({scrollTop:0}, "slow");
            return false;
        })
    };

    return {
        init:init
    }
});