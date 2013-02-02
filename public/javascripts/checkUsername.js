function checkUsername() {
    var username = $("#username").val();
    if (username === "")
        return {valid: false, message: "This field is required"};
    var result = JSON.parse($.ajax("/auth/signup/checkUsername", {
        type: "post",
        dataType: "text",
        data: {
            username: username
        },
        async: false
    }).responseText);
    if (result.available)
        return {valid: true, message: "That's a great username!"};
    else
        return {valid: false, message: "That username is alreay taken."}
}