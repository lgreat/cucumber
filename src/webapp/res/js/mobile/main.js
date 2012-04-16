var GS = GS || {};

require.config(GS.requireConfig);

require(['mobile'], function(mobile) {
    GS.log('main mobile module and its dependencies loaded.');
    mobile.init();
});

