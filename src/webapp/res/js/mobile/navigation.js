//when 'navigation' module is "require()d", execution will begin as soon as 'jquery' is available
define(function() {
    var $container = $('.js-mobile-header');
    var $goToButton = $('.top-nav-goto');

    var setupEventHandlers = function() {
        $(function() {
            $goToButton.on('click', ':visible', function() {
                $('.mobilePage:visible .mobile-sub-nav').toggle();
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