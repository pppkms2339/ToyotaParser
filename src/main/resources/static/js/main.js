function parse() {
    if (confirm("Парсинг сайта может занять много времени. Вы уверены?")) {
        document.getElementById('parseBtn').style.display = 'none';
        document.getElementById('parseBtnLoad').style.display = 'block';
        let xhr = new XMLHttpRequest();
        let url = path + "parse";
        xhr.open("GET", url, true);
        xhr.onload = function () {
            document.getElementById('parseBtn').style.display = 'block';
            document.getElementById('parseBtnLoad').style.display = 'none';
            location.replace(path);
        }
        xhr.send();
        //fetch(url).then(location.replace(path));
    }
}

function sendRequest(method, url, body = null) {
    return fetch(url)
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