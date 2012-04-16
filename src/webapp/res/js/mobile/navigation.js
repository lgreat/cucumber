//when 'navigation' module is "require()d", execution will begin as soon as 'jquery' is available
define(function() {
    var $container = null;

    $(function() {
        $container = $('.js-mobile-header');
    });

    var setupEventHandlers = function() {
        $(function() {
            $('.top-nav-goto').on('click', ':visible', function() {
                $('.mobilePage:visible .mobile-sub-nav').toggle();
                return false;
            });
        })
    };

    var init = function() {
        setupEventHandlers();
    };

    var $getContainer = function() {
        return $container.filter(':visible');
    };

    return {
        $getContainer:$getContainer,
        init:init
    };
});