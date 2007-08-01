function startRatingInit(listName) {
    var hidden_field = listName;
    var ul = document.getElementById("starRating_"+listName);
    var children = ul.getElementsByTagName("LI");
    for (var i=1; i < children.length; i++) {
        var li = children[i];
        var anchor = li.firstChild;
        if (anchor) {
            anchor.index = i;
            li.firstChild.onclick = function() {
                document.getElementById(hidden_field).value = this.index;
                this.hideFocus=true;//so ie won't outline stars
                return false;
            }
            li.firstChild.onmouseover = function() {
                this.parentNode.parentNode.getElementsByTagName("LI")[0].style.width = 20*this.index + '%';
            }
            li.firstChild.onmouseout = function() {
                this.parentNode.parentNode.getElementsByTagName("LI")[0].style.width = 20*document.getElementById(hidden_field).value + '%';
            }
        }
    }
}