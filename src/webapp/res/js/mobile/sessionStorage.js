define(function() {
    var namespace = 'GS.';
    var enabled = typeof(Storage) !== "undefined";

    var setItem = function(key,value) {
        if (typeof value === "object") {
            value = JSON.stringify(value);
        }

        sessionStorage.setItem(namespace + key,value);
    };

    var getItem = function(key) {
        var value = sessionStorage.getItem(namespace + key);

        if (value === null) {
            return null;
        }

        if (value[0] === '{') {
            value = JSON.parse(value);
        }

        return value;
    };

    return {
        enabled:enabled,
        setItem:setItem,
        getItem:getItem
    }

});