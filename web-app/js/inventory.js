if(typeof openboxes === "undefined") openboxes = {};
if(typeof openboxes.inventory === "undefined") openboxes.inventory = {};

openboxes.inventory.InventoryItem = function(attrs) {
    var self = this;
    if(!attrs) attrs = {};
    self.id = ko.observable(attrs.inventoryItemId);
    self.lotNumber = ko.observable(attrs.lotNumber);
    self.expirationDate = ko.observable(attrs.expirationDate);
    self.previousQuantity = ko.observable(attrs.quantityOnHand);
    self.currentQuantity = ko.observable(attrs.quantityOnHand);
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
            new openboxes.inventory.InventoryItem()
        );
    };

    self.removeItem = function(item){
        self.inventoryItems.remove(item);
        if(self.inventoryItems().length == 0)
            self.addItem();
    };

    if(self.inventoryItems().length == 0){
        self.addItem();
    }
};
