define(function() {

    var init = function(el) {
        // initialize and individual element
        if (el) {
            $('p.js-truncate', el).click(function(){
                expand(this);
            });
        }
        // initialize for entire document
        else {
            $(document).ready(function(){
                $('p.js-truncate').each(function(){
                    $(this).click(function(){
                        expand(this);
                    });
                });
            });
        }
    };

    var expand = function(el) {
        $('a.js-truncate-more',el).hide();
        $('span:nth-child(2)',el).show().css('display','inline');
    };


    return {
        init:init
    }
});
