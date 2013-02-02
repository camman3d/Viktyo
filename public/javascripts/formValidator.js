function validateForm($form) {
    var validationResult = true;
    try {
        $form.find(".required").each(function() {
            $(this).parent().parent().removeClass("error").removeClass("success");
            $(this).parent().children(".help-inline").text("");

            var validator = $(this).attr("data-validate");
            var valid = true;
            if (validator === "") {
                valid = $(this).val().length > 0;

                if (!valid) {
                    $(this).parent().parent().addClass("error");
                    $(this).parent().children(".help-inline").text("This field is required");
                } else {
                    $(this).parent().parent().removeClass("error");
                    $(this).parent().children(".help-inline").text("");
                }
            } else {
                if (validator.substr(0,5) === "match") {
                    var other = $("#" + validator.substr(6)).val();
                    valid = $(this).val() === other;

                    if (!valid) {
                        $(this).parent().parent().addClass("error");
                        $(this).parent().children(".help-inline").text("This field does not match.");
                    }
                }
                if (validator.substr(0,10) === "javascript") {
                    var code = validator.substr(11);
                    var result = eval(code);
                    valid = result.valid;

                    if (!valid) {
                        $(this).parent().parent().addClass("error");
                        $(this).parent().children(".help-inline").text(result.message);
                    } else if (result.message !== "") {
                        $(this).parent().parent().addClass("success");
                        $(this).parent().children(".help-inline").text(result.message);
                    }
                }
            }
            validationResult = validationResult && valid;
        });
        return validationResult;
    } catch(e) {
        console.error(e.message);
        return false;
    }
}

$(function() {
    $(".validate").submit(function() {
        return validateForm($(this));

    });
});