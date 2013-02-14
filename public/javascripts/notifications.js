$(function() {
    $(".notification.unread").click(function() {
        var $this = $(this);
        var id = $this.attr("data-id");
        $.ajax("/account/notifications/read", {
            type: "post",
            data: {
                notification: id
            },
            dataType: "json",
            success: function(data) {
                $this.removeClass("unread");
                var $notificationCount = $("#notificationCount");
                var count = + $notificationCount.text();
                $notificationCount.text(Math.max(count-1, 0));
            },
            error: function(data) {
                console.error("Error marking notification. " + JSON.stringify(data));
            }
        });
    });
});