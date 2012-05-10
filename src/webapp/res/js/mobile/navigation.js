//when 'navigation' module is "require()d", execution will begin as soon as 'jquery' is available
define(function() {
    var $container = null;

    $(function() {
        $container = $('.js-mobile-header');
    });

    var setupEventHandlers = function() {
        $(function() {
            $('.top-nav-goto').on('click', ':visible', function() {
                $('#mobile-sub-nav').toggle();
                return false;
            });

            $('#sub-nav-findSchool').click( function(){
                if (s.tl) {s.tl(this,'o', 'Mobile_Global_Search');}
            });

            $('#sub-nav-mss').click( function(){
                if (s.tl) {s.tl(this,'o', 'Mobile_Global_Saved_Schools');}
            });

            $('#sub-nav-nlSubscribe').click( function(){
                if (s.tl) {s.tl(this,'o', 'Mobile_Global_Newsletter');}
            });


        })
    };

    var init = function() {
        setupEventHandlers();
        setTimeout(function() {
            // Hide the address bar!
            window.scrollTo(0, 1);
        }, 0);
    };

    var $getContainer = function() {
        return $container.filter(':visible');
    };

    return {
        $getContainer:$getContainer,
        init:init
    };
});