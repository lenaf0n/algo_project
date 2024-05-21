$(document).ready(function(){
    // Event listener for custom-link clicks
    $('#linkContainer .custom-link').click(function(e) {
        e.preventDefault();
        $('#linkContainer .custom-link').removeClass('selected');
        $(this).addClass('selected');
    });

    // Load My Posts content on DOM ready
    loadMyPostsContent();

    // Function to load My Posts content
    function loadMyPostsContent() {
        const userId = $("#userDiv").attr("data-userId");
        $('#content').css('display', 'block');
        console.log(userId);
        $.ajax({
            url: '/post/' + userId,
            method: 'GET',
            success: function(data) {
                $('#content').html(data);
                $('#linkContainer .custom-link').removeClass('selected');
                $('#postsLink').addClass('selected');
            },
            error: function() {
                alert('Error loading the form');
            }
        });
    }

    function removeRequest() {
        const userId = $("#userDiv").attr("data-userId");
        console.log("Removing friend with user ID:", userId);
        $.ajax({
            type: 'DELETE',
            url: '/unlike/' + userId,
            success: function (response) {
                alert(response);
                location.reload();
            },
            error: function (xhr, status, error) {
                console.error(error);
            }
        });
    }

    // Event listener for clicking Posts link
    $('#postsLink').click(function(event){
        event.preventDefault();
        $('#content').removeClass('graph-background');
        $('#graph-container').css('display', 'none');
        loadMyPostsContent(); // Use the function to load "My Posts" content
    });

    // Event listener for clicking Remove Friend link
    $('#removeFriend').click(function(event){
        event.preventDefault();
        removeRequest();
    });

    $('#removePending').click(function(event){
        event.preventDefault();
        removeRequest();
    });

    $('#graphLink').click(function(event){
        event.preventDefault();
        $('#content').css('display', 'none');
        $('#graph-container').addClass('graph-background').css('display', 'block');
    });

    // Event listener for clicking Like Friend link
    $('#likeFriend').click(function(event){
        event.preventDefault();
        const userId = $("#userDiv").attr("data-userId");
        console.log("Liking friend with user ID:", userId);
        $.ajax({
            type: 'POST',
            url: '/like/' + userId,
            success: function (response) {
                alert(response);
                location.reload();
            },
            error: function (xhr, status, error) {
                console.error(error);
            }
        });
    });
});
