
// Dropdown menu positioning
/*function positionDropdown() {
    var pos = $("#header-user-image").offset();
    $("#dropdown-menu").offset({left: pos.left - 9, top: pos.top - 9});
}
$(window).load(function() {
    positionDropdown()
});*/

// Footer positioning
function positionFooter() {
    var w = $(window).height();
    var b = $("body").height();
    if($("#footer").hasClass("fixed"))
        b += 80;
    if(b < w )
        $("#footer").addClass("fixed");
    else
        $("#footer").removeClass("fixed");
}
$(window).ready(positionFooter);

$(function() {
    // Set up the dropdown menu
    /*positionDropdown();
    $("#dropdown-menu").hide();
    $(window).resize(function() {
        positionDropdown();
    });
    $("#header-user").click(function(event) {
        $("#dropdown-menu").show();
        positionDropdown();
        event.stopPropagation();
    });
    $('html').click(function() {
        $("#dropdown-menu").hide();
    });
    $("#header-user-dropdown").click(function() {
        $("#dropdown-menu").hide();
    });
    $('#dropdown-menu').click(function(event){
        event.stopPropagation();
    });*/

    // Set up the modals
    //$("#loginModal").modal({show:false});
    $("#feedbackModal").modal({show: false});
    $("#feedbackSubmit").click(function() {
        var email = $("#feedbackEmail").val();
        var subject = $("#feedbackSubject").val();
        var comments = $("#feedbackComments").val();
        $.ajax({
            url: "/feedback",
            data:{
                email: email,
                subject: subject,
                comments: comments
            },
            type: "post",
            success: function() {
                $("#feedbackModal").modal("hide");
                $("#feedbackField").val("");
            },
            error: function() {
                $("#feedbackModal").modal("hide");
                alert("error");
            }
        });
    });

    // Set up footer
    positionFooter();
    $(window).resize(positionFooter);
});
