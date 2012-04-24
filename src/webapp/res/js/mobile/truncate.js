define(function() {

    var init = function() {
        $('body').on('click', '.js-truncate', function(){
            $('a.js-truncate-more',this).hide();
            $('span:nth-child(2)',this).show().css('display','inline');
            return false;
        });
    };

    return {
        init:init
    }
});
