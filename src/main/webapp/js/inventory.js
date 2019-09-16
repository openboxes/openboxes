if(typeof openboxes === "undefined") openboxes = {};
if(typeof openboxes.inventory === "undefined") openboxes.inventory = {};

openboxes.inventory.InventoryItem = function(inventoryItem) {
    var self = this;
    if(!inventoryItem) inventoryItem = {};
    self.id = ko.observable(inventoryItem.inventoryItemId);
    self.lotNumber = ko.observable(inventoryItem.lotNumber);
    self.expirationDate = ko.observable(inventoryItem.expirationDate);
    self.previousQuantity = ko.observable(inventoryItem.quantityOnHand);
    self.currentQuantity = ko.observable(inventoryItem.quantityOnHand);
    
    
};

openboxes.inventory.RecordInventoryViewModel = function(product, inventoryItemsData) {
    var self = this;
    self.productId = ko.observable(product.id);
    self.productName = ko.observable(product.name);
    self.inventoryItems = ko.observableArray([]);
    _.each(inventoryItemsData, function(inventoryItem){
        self.inventoryItems.push(new openboxes.inventory.InventoryItem(inventoryItem));
    });

    self.addItem = function () {
        self.inventoryItems.push(
            new openboxes.inventory.InventoryItem({quantityOnHand:0})
        );
    };

    
    self.removeItem = function(item){
        self.inventoryItems.remove(item);
        if(self.inventoryItems().length == 0)
            self.addItem();
    };
    
    self.save = function (formElement) { 
    	var jsonString = ko.toJSON(self.inventoryItems);
    	jQuery.ajax({
            url: formElement.action,
            contentType: 'text/json',
            type: "POST",
            data: jsonString,
            async: false,
            dataType: "json",
            success: function(result) {
                console.log("success");
            },
            error: function() {
                console.log("failure");
            }
        });    	
    	
    	
    }
    
    

    if(self.inventoryItems().length == 0){
        self.addItem();
    }
};
