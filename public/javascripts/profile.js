$(function() {
    $(".profileInformation").submit(function() {
        // For each field
        $(this).find("input[type='text']").each(function() {
            // Save it if not empty
            var $this = $(this);
            var value = $this.val();
            if (value !== "") {
                $.ajax("/account/setProperty", {
                    type: "post",
                    data: {
                        attribute: $this.attr("id"),
                        value: value
                    },
                    dataType: "json",
                    error: function(data) {
                        console.error("Error saving property. " + JSON.stringify(data));
                    }
                });
            }
        });
        return false;
    });
});