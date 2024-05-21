$(document).ready(function() {
    $('.comments-btn').click(function() {
        const postId = $(this).attr('data-postId');
        const postCotent = $(this).attr('data-post');
        $.ajax({
            type: "GET",
            url: "/comments/" + postId,
            success: function(comments) {
                $('#comments').empty();

                $('#post-content').text(postCotent);

                comments.forEach(function(comment) {
                    const commentElement = $('<div>').addClass('comment px-2 py-2 mb-3');
                    const usernameElement = $('<strong>').text('@' + comment.username + ' : ');
                    const contentElement = $('<span>').text(comment.content);
                    commentElement.append(usernameElement).append(contentElement);
                    $('#comments').append(commentElement);
                });

                $('#sticky-sidebar').show();
            },
            error: function(xhr, status, error) {
                console.error("Error fetching comments:", error);
            }
        });
    });
});



$(document).ready(function () {
    $('.like-btn').click(function () {
        const postId = $(this).data('postid');
        const liked = $(this).data('liked');
        const url = liked ? "/post/unlike/" + postId : "/post/like/" + postId;

        $.ajax({
            url: url,
            type: liked ? "DELETE" : "POST",
            success: function (response) {
                if (liked) {
                    location.reload();
                } else {
                    location.reload();
                }
            },
            error: function (xhr, status, error) {
                console.error(error);
                // Handle errors here
            }
        });
    });
});

$(document).ready(function() {
    $('#comment-form').submit(function(event) {
        // Prevent default form submission
        event.preventDefault();

        // Get the comment content
        const comment = $('#commentContent').val();
        console.log(comment);

        // Construct the AJAX request URL
        const url = '/post/comment/' + encodeURIComponent(comment);

        // Send AJAX request
        $.ajax({
            type: 'POST',
            url: url,
            success: function(response) {
                location.reload();
                console.log('Comment submitted successfully.');
            },
            error: function(xhr, status, error) {
                // Handle error
                console.error('Error submitting comment:', error);
            }
        });
    });
});
