var searchOpen = false;

function openSearch() {
    $("#searchOverlay").fadeIn(500, function() {
        $("#searchForm").children("input[type='text']").focus();
        searchOpen = true;
    });
}

function closeSearch() {
    $("#searchOverlay").fadeOut(500);
    searchOpen = false;
}

function getKeyCode(evt) {
    var keycode = null;
    if(window.event) {
        keycode = window.event.keyCode;
    } else if(evt) {
        keycode = evt.which;
    }
    return keycode;
}

function searchKeyHandler(evt) {
    if (!searchOpen)
        return;
    var keycode = getKeyCode(evt);

    // ESC closes search
    if (keycode === 27)
        closeSearch();
}

$(function() {
    // Set up the search link
    $("#searchLink").click(function() {
        openSearch();
    });

    // Search is hidden by default
    $("#searchOverlay").hide();

    // Bind key handler
    $(document).keyup(searchKeyHandler);

    // Hide the results
    var $searchResults = $("#searchResults").hide();

    $("#searchForm").submit(function() {
        $.ajax("/search", {
            type: "post",
            data: {
                search: $("#searchForm").children("input[type='text']").val()
            },
            dataType: "json",
            success: function(data) {
                var i;

                // Show the results
                $searchResults.show();
                $postingsUl = $searchResults.find(".span4:nth-child(1) > ul");
                for(i=0; i<data.postings.length; i++) {
                    var posting = data.postings[i];
                    $postingsUl.append("<li><a href='/posting/" + posting.id + "'>" + posting.name + "</a></li>");
                }

                $orgsUl = $searchResults.find(".span4:nth-child(2) > ul");
                for(i=0; i<data.organizations.length; i++) {
                    var organization = data.organizations[i];
                    $orgsUl.append("<li><a href='/users/" + organization.id + "'>" + organization.fullname + "</a></li>");
                }

                $usersUl = $searchResults.find(".span4:nth-child(3) > ul");
                for(i=0; i<data.users.length; i++) {
                    var user = data.users[i];
                    $usersUl.append("<li><a href='/users/" + user.id + "'>" + user.fullname + "</a></li>");
                }
            }
        });
        return false;
    });
});