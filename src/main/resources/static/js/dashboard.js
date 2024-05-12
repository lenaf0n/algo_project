$(document).ready(function () {
    $('#searchInput').on('input', function () {
        let username = $(this).val();
        if (username.trim() !== '') {
            $.ajax({
                type: 'GET',
                url: '/search/' + username,
                success: function (data) {
                    $('#userList').empty();
                    if (data.length <= 10) {
                        console.log(data);
                        data.forEach(function (searchResultUser) {
                            let listItem = $('<li>').addClass('list-group-item d-flex justify-content-between align-items-center');
                            let nameUsernameContainer = $('<div>').addClass('col');
                            let name = $('<strong>').text(searchResultUser.user.name).addClass('pl-1');
                            let username = $('<span>').addClass('text-muted').text('@' + searchResultUser.user.username);
                            nameUsernameContainer.append(name, username);
                            let heartButton = $('<button>').addClass('btn btn-sm');

                            if (searchResultUser.friend) {
                                heartButton.addClass('btn-danger').html('<i class="bi bi-heart-fill"></i>');
                            } else {
                                heartButton.addClass('btn-outline-danger').html('<i class="bi bi-heart"></i>');
                            }

                            heartButton.click(function () {
                                if ($(this).hasClass('btn-danger')) {
                                    filledHeartFunction(searchResultUser.user.id);
                                } else {
                                    outlineHeartFunction(searchResultUser.user.id);
                                }
                            });
                            listItem.append(nameUsernameContainer, heartButton);
                            $('#userList').append(listItem);
                        });
                    }
                },
                error: function (xhr, status, error) {
                    console.error(error);
                }
            });
        } else {
            $('#userList').empty();
        }
    });

    function filledHeartFunction(userId) {
        console.log('filled')
        $.ajax({
            type: 'DELETE',
            url: '/unlike/' + userId,
            success: function (response) {
                closePopup();
                alert(response);
            },
            error: function (xhr, status, error) {
                console.error(error);
            }
        });
    }

    function outlineHeartFunction(userId) {
        console.log('outline')
        $.ajax({
            type: 'POST',
            url: '/like/' + userId,
            success: function (response) {
                closePopup();
                alert(response);
            },
            error: function (xhr, status, error) {
                console.error(error);
            }
        });
    }

    $('#searchModal').on('hidden.bs.modal', function () {
        $('#searchInput').val('');
        $('#userList').empty();
    });
});

function closePopup() {
    $('#searchModal').modal('hide');
}