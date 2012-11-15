//if(typeof process_requisition === "undefined") process_requisition = {};
//
//process_requisition.Requisition = function(id, requisitionItems) {
//    var self = this;
//    self.id = ko.observable(id);
//    self.requisitionItems = ko.utils.arrayMap(requisitionItems, function(item) {
//        return new process_requisition.RequisitionItem
//        (
//            item.id,
//            item.product,
//            item.quantity,
//            item.substitutable,
//            item.recipient,
//            item.comment,
//            item.orderIndex,
//            item.picklistItems );
//    });
//}
//
//process_requisition.Product = function(id, name)  {
//    var self = this;
//    self.id = ko.observable(id);
//    self.name = ko.observable(name);
//
//}
//
//process_requisition.RequisitionItem = function(id, product, quantity, substitutable, recipient, comment, orderIndex, picklistItems) {
//    console.log("Found a product requisition.");
//    var self = this;
//    self.id = ko.observable(id);
//    self.product = ko.observable(product); //new process_requisition.Product(product.id, "something here");//    ko.observable(product);
//    self.quantity = ko.observable(quantity);
//    self.substitutable = ko.observable(substitutable);
//    self.orderIndex = ko.observable(orderIndex);
//    self.quantityPicked = ko.observable(0);
//    self.quantityRemaining = ko.observable(quantity);
//    self.picklistItems = ko.observable(picklistItems);
//
//    self.status = ko.computed(function() {
//        if(self.quantityPicked == 0) return "Incomplete";
//        if(self.quantityPicked >= self.quantity) return "Complete";
//        return "PartiallyComplete";
//    });
//}
//
//process_requisition.InventoryItem = function(id) {
//    var self = this;
//    self.id = ko.observable(id);
//}
//
//process_requisition.ViewModel = function(requisition) {
//    var self = this;
//    self.requisition = requisition;
//};
