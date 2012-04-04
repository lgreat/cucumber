({
    appDir: "../../src/webapp/res/js/mobile",
    baseUrl: ".",
    // let's place compiled/optimized/minified JS files into target directory before war is built
    dir: "../../target/gs-web/res/js/mobile",

    // for each jspx page, enter page-specific module name below so that it gets compile/minified
    modules: [
        /*{
            name: "main",
            exclude: ["global","s_code"]
        },
        {
            name: "index",
            exclude: ["global","s_code"]
        },
        {
            name: "mobileSearchResults",
            exclude: ["global","s_code"]
        }*/
    ],

    paths: {
        'global':'../global',
        'sCode':'../s_code_dev'
    },

    packages: ["tracking"]
})