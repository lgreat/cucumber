({
    appDir: "../../src/webapp/res/js/mobile",
    baseUrl: ".",
    // let's place compiled/optimized/minified JS files into target directory before war is built
    dir: "../../target/gs-web/res/js/mobile",

    // for each jspx page, enter page-specific module name below so that it gets compile/minified
    modules: [
        {
            name: "main",
            include: ['subCookie','nearbyCitiesList','nearbyDistrictsList']
        },
        {
            name: "index"
        },
        {
            name: "mobileSearchResults"
        },
        {
            name: "parentReviews"
        },
        {
            name: "testScores"
        },
        {
            name: "truncate"
        },
        {
            name: "schoolOverview"
        }
    ],

    paths: {
        'sCode':'empty:',
        'async':'./plugins/async',
        'order':'./plugins/order'
    },

    packages: ["tracking"]
})