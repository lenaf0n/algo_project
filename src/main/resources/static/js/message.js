function postMessage(inventoryCell, quantity){
    const divToShow = document.getElementById("sellItemPanel");
    divToShow.style.visibility = 'visible';
    const inventoryId = inventoryCell.id
    console.log("inventory Id = " + inventoryId);
        $.ajax({
            type: 'GET',
            url: '/inventory/' + inventoryId,
            success: function(response) {
                console.log(response);

                const item = response;

                console.log('Item = ' + item);
                console.log("Item Name = " + item.name);
                console.log("Item Image = " + item.urlImage);
                console.log("Item Id = " + item.id);
                console.log("Quantity = " + quantity);

                $('#item-name').text(item.name);
                $('#item-image').text(item.urlImage);
                $('#quantity').attr('max', quantity);
                $('#idItem').attr('value', item.id);
            },
            error: function(xhr, status, error) {
                console.error('Error: ', error);
            }
        });
}