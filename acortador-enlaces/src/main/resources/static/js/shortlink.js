function subirlink() {
    var link = $("#link").val();
    $.ajax({
        url: "/data/add", method: "post", data: {"link": link}, dataType: "json",
        success: function (data) {
            $("#shortlinks").append();
            console.log(data);
        }
    });
}