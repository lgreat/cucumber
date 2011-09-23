GS = {};

GS.Person = function() {};

GS.Person.prototype.speak = function(words) {
    console.log("GS.Person.speak called with: " + words);
    return words;
};