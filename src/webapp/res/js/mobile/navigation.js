//when 'navigation' module is "require()d", execution will begin as soon as 'jquery' is available
define(['jquery'], function($) {
    var $container = $('#standard-mobile-header');
    var $goToButton = $('#top-nav-goto');
    var $subNav = $('#mobile-sub-nav');

    var setupEventHandlers = function() {
        $goToButton.on('click', function() {
            $subNav.toggle();
        });
    };

    var init = function() {
        setupEventHandlers();
    };

    return {
        $container:$container,
        $goToButton:$goToButton,
        $subNav:$subNav,
        init:init
    };
});