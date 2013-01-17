$(document).ready(function () {

    $("#status").html("<p class='muted'>Waiting Events</p>  ");

    // ***********************
    // Define Mustache template
    // ***********************
    var statsTemplate = null;
    $.get('/assets/templates/mustache-stats.html', function (d) {
        statsTemplate = d;
    });


    // ***********************
    // Define Server-sent events
    // ***********************

    var feed;

    if (window.EventSource) {
        feed = new EventSource('/stream');

        // Connection was opened.
        feed.addEventListener('open', function (e) {
            $("#status").html("Connection open");
        }, false);

        feed.addEventListener('dbInfo', function (e) {
            var data = JSON.parse(e.data);
            console.log("data received : " + data)
            renderStats(data)

        }, false);


        feed.addEventListener('error', function (e) {
            if (e.readyState == EventSource.CLOSED) {
                $("#status").html("Connection closed");
            } else {
                $("#status").html("Unknown error" + e);
            }
        }, false);

    } else {
        $("#status").html("Serverside Send Event not supported by this browser");
    }

    function renderStats(data) {
        var statsRendered = Mustache.to_html(statsTemplate, data);
        $("#status").html(statsRendered);
    }
})