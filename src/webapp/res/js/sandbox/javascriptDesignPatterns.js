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
    var z = 0;
    return {
        _bumpZ: function() {
            z++;
        },
        _alertZ: function() {
            alert(z);
        }
    };
};
GS.javascriptDesignPatterns.Class = function(_x, _y) {
    var selector = '.js_' + _x + '_' + _y;
    var bumpZ = this._bumpZ;
    var alertZ = this._alertZ;
    var nonPrototypalMethod = function() {
        alert(_x + "," + _y);
    };
    var bumpZAndAlert = function() {
        bumpZ();
        alertZ();
    };
    var callOtherMethod = function() {
        nonPrototypalMethod();
    };
    jQuery(selector).on('click', callOtherMethod);
    return {
        x: _x,
        y: _y,
        alertXY: callOtherMethod,
        bumpZAndAlert: bumpZAndAlert,
        bumpZ: bumpZ,
        alertZ: alertZ
    };
};
GS.javascriptDesignPatterns.Class.prototype = new GS.javascriptDesignPatterns.ClassPrototype();

var first, second;
jQuery(function() {
    first = new GS.javascriptDesignPatterns.Class(1,2);
    second = new GS.javascriptDesignPatterns.Class(3,4);
});


////////////////////////////
// JavaScript Hoising     //
////////////////////////////
// In JavaScript, all function and variable definitions are "hoisted" to the top of their containing scope, as though
// you declared them as undefined on line 1 of the function (var myvar;). However, function declarations and function
// expressions behave differently as in the example from JavaScript Patterns below:

// Example from "JavaScript Patterns" by Stoyan Stefanov
// antipattern for illustration only

// global functions
function foo() {
    alert('global foo');
}
function bar() {
    alert('global bar');
}

function hoistMe() {
    console.log(typeof foo); // "function"
    console.log(typeof bar); // "undefined"

    foo(); // "local foo"
    //bar(); // TypeError: undefined is not a function

    // function declaration:
    // variable 'foo' and its implementation both get hoisted
    function foo() {
        alert('local foo');
    }

    // function expression:
    // only variable bar gets hoisted and not the implementation
    var bar = function() {
        alert('local bar');
    };
}
hoistMe();


// a named function expression. function name is equal to variable that references the function.
// named function expressions contain a .name property, which is not part of the language, but available in many browsers.
var namedFunctionExpression = function namedFunctionExpression() {
    alert('named function expression. name=' + namedFunctionExpression.name);
};

// an unnamed function expression. A.K.A. anonymous function
var unnamedFunctionExpression = function() {
    alert('unnamed function expression. name=' + unnamedFunctionExpression.name);
};

// a function declaration. Function declarations don't require a semicolon after the closing curly brace, but function
// expressions do. Function declarations have a .name property just like named function expressions
function functionDeclaration() {
    alert('A function declaration. name=' + functionDeclaration.name);
}

/**
 * Brackets tell idea the parameter is optional
 * @param [redirect]
 * @param [schoolName]
 * @param [schoolId]
 * @param [schoolState]
 */
function configureSchoolInfo_old(redirect, schoolName, schoolId, schoolState) {
    if (redirect) {
        // do something
    }
    if (schoolName) {
        // do something
    }
    // etc.
}

/**
 * @param options.redirect
 * @param options.schoolName
 * @param options.schoolId
 * @param options.schoolState
 */
function configureSchoolInfo_new(options) {
    if (typeof options.redirect !== 'undefined') {
        console.log("redirect");
    }
    if (options.schoolName) {
        console.log("schoolName");
    }
    // etc.
}

configureSchoolInfo_old();
configureSchoolInfo_new({redirect: 'foo'});
