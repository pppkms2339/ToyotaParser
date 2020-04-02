function parse() {
    if (confirm("Парсинг сайта может занять много времени. Вы уверены?")) {
        document.getElementById('parseBtn').style.display = 'none';
        document.getElementById('parseBtnLoad').style.display = 'block';
        let req = initRequest();
        let url = path + "parse";
        req.open("GET", url, false);
        req.onload = function () {
            location.replace(path);
        };
        req.send(null);
    }
}

function initRequest() {
    if (window.XMLHttpRequest) {
        if (navigator.userAgent.indexOf('MSIE') != -1) {
            isIE = true;
        }
        return new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        isIE = true;
        return new ActiveXObject("Microsoft.XMLHTTP");
    }
}

$(document).ready(function() {
    $("#searchBtn").on("click", function(event) {
        event.preventDefault();
        let menuInputValue = $("#searchText").val();
        if(menuInputValue != '') {
            location.replace(path + 'searchBodyNumber?param=' + menuInputValue);
        }
    });

    $('#searchText').keypress(function(event) {
        let keycode = (event.keyCode ? event.keyCode : event.which);
        if(keycode == '13') {
            event.preventDefault();
            let menuInputValue = $("#searchText").val();
            if(menuInputValue != '') {
                location.replace(path + 'searchBodyNumber?param=' + menuInputValue);
            }
        }
    });
});