define(function() {
    var init = function() {
        $(document).ready(function(){
            $('p.js-truncate').each(function(){
                $(this).click(function(){
                    $('a.js-truncate-more',this).hide();
                    $('span.js-truncate-collapsable',this).show().css('display','inline');
                });
            });
        });
    };
    return {
        init:init
    }
});
