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
                        data.forEach(function (searchResultUser) {
                            let listItem = $('<li>').addClass('list-group-item d-flex justify-content-between align-items-center');
                            let nameUsernameContainer = $('<div>').addClass('col');
                            let name = $('<strong>').text(searchResultUser.user.name).addClass('pl-1');
                            let username = $('<span>').addClass('text-muted').text('@' + searchResultUser.user.username);
                            nameUsernameContainer.append(name, username);
                            let heartButton = $('<button>').addClass('btn btn-sm');

                            if (searchResultUser.status === 'FRIEND') {
                                heartButton.addClass('btn-danger').html('<i class="bi bi-heart-fill"></i>');
                            } else if (searchResultUser.status === 'PENDING') {
                                heartButton.addClass('btn-warning').attr('disabled', true).html('<i class="bi bi-heart"></i>');
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
        $.ajax({
            type: 'DELETE',
            url: '/unlike/' + userId,
            success: function (response) {
                closeSearchPopup();
                alert(response);
                location.reload();
            },
            error: function (xhr, status, error) {
                console.error(error);
            }
        });
    }

    function outlineHeartFunction(userId) {
        $.ajax({
            type: 'POST',
            url: '/like/' + userId,
            success: function (response) {
                closeSearchPopup();
                alert(response);
                location.reload();
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

function closeSearchPopup() {
    $('#searchModal').modal('hide');
}

function fetchNotifications() {
    $.ajax({
        url: '/user/notifications',
        method: 'GET',
        success: function(response) {
            if (response.length > 0) {
                updateNotificationsLink(true);
                $('#connectionList').empty();
                response.forEach(function (connection) {
                    let listItem = $('<li>').addClass('list-group-item d-flex justify-content-between align-items-center');
                    let nameUsernameContainer = $('<div>').addClass('col');
                    let name = $('<strong>').text(connection.name).addClass('pl-1');
                    let username = $('<span>').addClass('text-muted').text('@' + connection.username);
                    nameUsernameContainer.append(name, username);
                    let acceptButton = $('<button>').addClass('btn btn-sm').text('Accept connection');

                    acceptButton.click(function () {
                        accpetConnection(connection.id);
                    });
                    listItem.append(nameUsernameContainer, acceptButton);
                    $('#connectionList').append(listItem);
                });
            } else {
                updateNotificationsLink(false);
            }
        },
        error: function(xhr, status, error) {
            console.error('Error fetching notifications:', error);
        }
    });
}

function updateNotificationsLink(hasNotifications) {
    const notificationsLink = document.getElementById('notificationsLink');
    if (hasNotifications) {
        notificationsLink.classList.add('text-danger');
    } else {
        notificationsLink.classList.remove('text-danger');
    }
}

function accpetConnection(userId) {
    $.ajax({
        type: 'POST',
        url: '/accept-connection/' + userId,
        success: function (response) {
            alert(response);
            location.reload();
        },
        error: function (xhr, status, error) {
            console.error(error);
        }
    });
}

fetchNotifications();
setInterval(fetchNotifications, 60000);