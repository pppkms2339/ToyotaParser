function parse() {
    if (confirm("Парсинг сайта может занять много времени. Вы уверены?")) {
        let xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
          if (xhr.readyState < 4) {
            $('#parseBtn').css('display','none');
            $('#parseBtnLoad').css('display','block');
          }
        };
        xhr.onload = function() {
            location.replace(path);
        };
        let url = path + "parse";
        xhr.open("GET", url, true);
        xhr.send(null);
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