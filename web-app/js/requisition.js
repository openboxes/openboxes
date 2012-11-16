if(typeof warehouse === "undefined") warehouse = {};
warehouse.Requisition = function(attrs) {
    var self = this;
    if(!attrs) attrs = {};
    self.id = ko.observable(attrs.id);
    self.originId= ko.observable(attrs.originId);
    self.originName = ko.observable(attrs.originName);
    self.dateRequested = ko.observable(attrs.dateRequested);
    self.requestedDeliveryDate = ko.observable(attrs.requestedDeliveryDate);
    self.requestedById = ko.observable(attrs.requestedById);
    self.requestedByName = ko.observable(attrs.requestedByName);
    self.recipientProgram = ko.observable(attrs.recipientProgram);
    self.lastUpdated = ko.observable(attrs.lastUpdated);
    self.status = ko.observable(attrs.status);
    self.version = ko.observable(attrs.version);
    self.requisitionItems = ko.observableArray([]);
    self.name = ko.observable(attrs.name);

    for(var idx in attrs.requisitionItems){
      var item = new warehouse.RequisitionItem(attrs.requisitionItems[idx]);
      self.requisitionItems.push(item);
    }

    self.findRequisitionItemByOrderIndex = function(orderIndex){
      for(var idx in self.requisitionItems()){
        if(self.requisitionItems()[idx].orderIndex() == orderIndex) 
          return self.requisitionItems()[idx];
      }
    };

    self.newOrderIndex = function(){
      var orderIndex = -1;
      for(var idx in self.requisitionItems()){
        if(self.requisitionItems()[idx].orderIndex() > orderIndex) 
          orderIndex = self.requisitionItems()[idx].orderIndex();
      }
      return orderIndex + 1;
    }
 };

warehouse.RequisitionItem = function(attrs) {
    var self = this;
    if(!attrs) attrs = {};
    self.id = ko.observable(attrs.id);
    self.productId = ko.observable(attrs.productId);
    self.productName = ko.observable(attrs.productName);
    self.quantity =  ko.observable(attrs.quantity);
    self.comment = ko.observable(attrs.comment);
    self.substitutable =  ko.observable(attrs.substitutable);
    self.recipient = ko.observable(attrs.recipient);
    self.orderIndex = ko.observable(attrs.orderIndex);
};

warehouse.ViewModel = function(requisition) {
    var self = this;
    self.requisition = requisition;

    self.addItem = function () {
       self.requisition.requisitionItems.push(
        new warehouse.RequisitionItem({orderIndex:self.requisition.newOrderIndex()})
       );
    };

    self.removeItem = function(item){
       self.requisition.requisitionItems.remove(item);
       if(self.requisition.requisitionItems().length == 0)
         self.addItem();

    };

    self.save = function(formElement) {
        var data = ko.toJS(self.requisition);
        data["origin.id"] = data.originId;
        data["requestedBy.id"] = data.requestedById;
        delete data.version;
        delete data.status;
        delete data.lastUpdated;
        $.each(data.requisitionItems, function(index, item){
          item["product.id"] = item.productId;
        });
        var jsonString = JSON.stringify( data);
        console.log("here is the req: "  + jsonString);
        console.log("endpoint is " + formElement.action);
        jQuery.ajax({
            url: formElement.action,
            contentType: 'text/json',
            type: "POST",
            data: jsonString,
            dataType: "json",
            success: function(result) {
                console.log("result:" + JSON.stringify(result));
                if(result.success){
                    self.requisition.id(result.id);
                    self.requisition.status(result.status);
                    self.requisition.lastUpdated(result.lastUpdated);
                    self.requisition.version(result.version);
                    if(result.requisitionItems){
                      for(var idx in result.requisitionItems){
                        var localItem = self.requisition.findRequisitionItemByOrderIndex(result.requisitionItems[idx].orderIndex);
                        localItem.id(result.requisitionItems[idx].id);
                      }
                    }
                }
            }
        });

    };

    if(self.requisition.requisitionItems().length == 0 && self.requisition.id()){
            self.addItem();
    }

   
    self.requisition.id.subscribe(function(newValue) {
        if(newValue){
          self.addItem();
        }
    });

};

warehouse.saveRequisitionToLocal = function(model){
  var data = ko.toJS(model);
  if(!data.id) return null;
  var key = "warehouseRequisition" + data.id;
  warehouse.saveToLocal(key, data);
  return key;
}

warehouse.getRequisitionFromLocal = function(id){
  if(!id) return null;
  var key = "warehouseRequisition" + id;
  return warehouse.getFromLocal(key);
}

warehouse.saveToLocal = function(name, model){
  if(typeof(Storage) === "undefined") return;
  localStorage[name] = JSON.stringify(model);
};

warehouse.getFromLocal = function(name){
  if(typeof(Storage) !== "undefined" && localStorage[name])
    return JSON.parse(localStorage[name]);
  return null;
};



