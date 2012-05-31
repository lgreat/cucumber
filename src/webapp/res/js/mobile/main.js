var GS = GS || {};

if (GS.requireConfig) {
    require.config(GS.requireConfig);
}

require(['mobile'], function(mobile) {
    GS.log('main mobile module and its dependencies loaded.');
    mobile.init();
});

