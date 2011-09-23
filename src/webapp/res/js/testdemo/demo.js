GS = {};

GS.Person = function() {};

GS.Person.prototype.speak = function(words) {
    console.log("GS.Person.speak called with: " + words);
    return words;
};

GS.Person.prototype.usingGoodBrowser = function() {
    return navigator.appVersion.indexOf("Chrome") > -1;
}