/////////////////
// NAMESPACING //
/////////////////
// top-level
var GS = GS || {}; // top-level global variable must be prefaced with "var"

// package
GS.javascriptDesignPatterns = GS.javascriptDesignPatterns || {}; // packages and below do not need "var"

// module
GS.javascriptDesignPatterns.exampleModule = (function() {
    // functionality here
})();

// class
GS.javascriptDesignPatterns.ExampleClass = function() {
    // body here
};

// Notes:
// - Convention is for packages/modules to start lower-case and instantiatable objects to start upper-case, and both
//   should be camel-cased. A lower-cased identifier is something you call, while an upper-cased identifier
//   is something you instantiate with new.

////////////
// MODULE //
////////////
// See http://addyosmani.com/resources/essentialjsdesignpatterns/book/#modulepatternjavascript
GS.javascriptDesignPatterns.module = (function() {
    // private attributes
    var privateVar = 13;
    // private methods
    var doubleVar = function() {
        privateVar *= 2;
    };
    return {
        // public attributes
        publicVar : 25,
        // public methods
        myFunc : function() {
            alert(privateVar);
            doubleVar();
        },
        myOtherFunc : function() {
            alert(this.publicVar/5);
        }
    };
})();

// Pros:
// - Allows for public/private variables
// - Familiar structure to those coming from traditional OOP
// Cons:
// - declare public/private members differently
// - access public/private members differently (with/without 'this')
// - changing visibility is hard
// - methods added in at runtime don't have access to private members

//////////////////////
// REVEALING MODULE //
//////////////////////
// See http://addyosmani.com/resources/essentialjsdesignpatterns/book/#revealingmodulepatternjavascript
GS.javascriptDesignPatterns.revealingModule = (function() {
    // private attributes/methods
    var privateVar = 13;
    var publicVar = 25;

    var doubleVar = function() {
        privateVar *= 2;
    };

    var alertPrivateVarThenDoubleIt = function() {
        alert(privateVar);
        doubleVar();
    };

    var alertOneFifthPubVar = function() {
        alert(publicVar/5);
    };

    // interface
    return {
        publicVar: publicVar,
        myFunc: alertPrivateVarThenDoubleIt,
        myOtherFunc: alertOneFifthPubVar
    };
})();
// Pros:
// - Allows for public/private variables
// - Familiar structure to those coming from traditional OOP
// - declare public/private members the same
// - access public/private members the same (no 'this' to be seen!)
// - changing visibility is easy
// Cons:
// - methods added in at runtime don't have access to private members
// - unclear from interface what are vars and what are functions
// - unclear from interface what the signature of public functions is


////////////////////////////
// Prototypal Inheritance //
////////////////////////////
GS.javascriptDesignPatterns.ClassPrototype = function() {
    this.z = 0;
    this.prototypalMethod = function() {
        alert(this.x + "," + this.y);
    };
    this.bumpZ = function() {
        this.z++;
    };
};
GS.javascriptDesignPatterns.Class = function(x, y) {
    this.x = x;
    this.y = y;
    this.nonPrototypalMethod = function() {
        alert(this.x + "," + this.y);
    };
    this.bumpZAndAlert = function() {
        this.bumpZ();
        alert(this.z);
    };
};
GS.javascriptDesignPatterns.Class.prototype = new GS.javascriptDesignPatterns.ClassPrototype();

var first = new GS.javascriptDesignPatterns.Class(1,2);
var second = new GS.javascriptDesignPatterns.Class(3,4);
