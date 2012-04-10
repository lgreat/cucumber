({
    appDir: "../../src/webapp/res/js/mobile",
    baseUrl: ".",
    // let's place compiled/optimized/minified JS files into target directory before war is built
    dir: "../../target/gs-web/res/js/mobile",

    // for each jspx page, enter page-specific module name below so that it gets compile/minified
    modules: [
        {
            name: "main"
        },
        {
            name: "index"
        },
        {
            name: "mobileSearchResults"
        },
        {
            name: "testScores"
        },
        {
            name: "schoolOverview"
        }
    ],

    paths: {
        'global':'empty:',
        'sCode':'empty:',
        'async':'./plugins/async'
    },

    packages: ["tracking"]
})