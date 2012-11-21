if(typeof openboxes === "undefined") openboxes = {};
if(typeof openboxes.requisition === "undefined") openboxes.requisition = {};
openboxes.requisition.Requisition = function(attrs) {
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
      var item = new openboxes.requisition.RequisitionItem(attrs.requisitionItems[idx]);
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

openboxes.requisition.Picklist = function(attrs) {
    var self = this;
    if(!attrs) attrs = {};
    self.id = ko.observable(attrs.id);
    self.requisitionId = ko.observable(attrs.requisitionId);
};

openboxes.requisition.PicklistItem = function(attrs){
    var self = this;
    if(!attrs) attrs = {};
    self.id = ko.observable(attrs.id);
    self.requisitionItemId = ko.observable(attrs.requisitionItemId);
    self.inventoryItemId = ko.observable(attrs.inventoryItemId);
    self.lotNumber = ko.observable(attrs.lotNumber);
    self.expirationDate = ko.observable(attrs.expirationDate);
    self.quantityOnHand = ko.observable(attrs.quantityOnHand);
    self.quantityATP = ko.observable(attrs.quantityATP);
    self.quantityPicked = ko.observable(attrs.quantityPicked || 0);
};

openboxes.requisition.RequisitionItem = function(attrs) {
    var self = this;
    if(!attrs) attrs = {};
    self.id = ko.observable(attrs.id);
    self.version = ko.observable(attrs.version);
    self.productId = ko.observable(attrs.productId);
    self.productName = ko.observable(attrs.productName);
    self.quantity =  ko.observable(attrs.quantity);
    self.comment = ko.observable(attrs.comment);
    self.substitutable =  ko.observable(attrs.substitutable);
    self.recipient = ko.observable(attrs.recipient);
    self.orderIndex = ko.observable(attrs.orderIndex);
    self.picklistItems = ko.observableArray([]);

    self.quantityPicked = ko.computed(function() {
        var sum = 0;
        for(var i in self.picklistItems()) {
            sum += parseInt(self.picklistItems()[i].quantityPicked());
        }
        return sum;
    }, this);

    self.quantityRemaining = ko.computed(function() {
        var num = this.quantity() - this.quantityPicked();
        return (num > 0) ? num : 0
    }, this);

    self.status = ko.computed(function() {
        if(this.quantityPicked() == 0) return "Incomplete";
        if(this.quantityPicked() >= this.quantity()) return "Complete";
        return "PartiallyComplete";
    }, this);
};

openboxes.requisition.ProcessViewModel = function(requisitionData, picklistData, inventoryItemsData) {
    var self = this;
    self.requisition = new openboxes.requisition.Requisition(requisitionData);
    self.picklistId = picklistData.id;
    var getPicklistItem = function(inventoryItem, requisitionItemId){
      var picklistItem = new openboxes.requisition.PicklistItem(inventoryItem);
      picklistItem.requisitionItemId(requisitionItemId);
      for(var k in picklistData.picklistItems){
        var picklistItemData = picklistData.picklistItems[k];
        if(picklistItemData.inventoryItemId == inventoryItem.inventoryItemId && 
          picklistItemData.requisitionItemId == requisitionItemId){
          picklistItem.quantityPicked(picklistItemData.quantity);
          break;
        }
      }
      return picklistItem;
    }

    for(var i in self.requisition.requisitionItems()) {
        var requisitionItem = self.requisition.requisitionItems()[i];
        var inventoryItems = inventoryItemsData[requisitionItem.productId()];
        for(var j in inventoryItems) {
          var picklistItem = getPicklistItem(inventoryItems[j], requisitionItem.id());
          requisitionItem.picklistItems.push(picklistItem);
        };
    };

    self.save = function(formElement) {
        var data = ko.toJS(self.requisition);
        delete data.version;
        delete data.lastUpdated;
        $.each(data.requisitionItems, function(index, item){
            $.each(item.picklistItems, function(ind, pl) {
                pl["picklist.id"] = self.picklistId;
                pl["inventoryItem.id"] = pl.inventoryItemId;
                pl["requisitionItem.id"] = pl.requisitionItemId;
                pl["quantity"] = pl.quantityPicked;
            });
            delete item.version;
        });
        var jsonString = JSON.stringify(data);
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
                }
            }
        });
    };

};

openboxes.requisition.ViewModel = function(requisition) {
    var self = this;
    self.requisition = requisition;

    self.addItem = function () {
       self.requisition.requisitionItems.push(
        new openboxes.requisition.RequisitionItem({orderIndex:self.requisition.newOrderIndex()})
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
          delete item.version;
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
                    self.requisition.id(result.data.id);
                    self.requisition.status(result.data.status);
                    self.requisition.lastUpdated(result.data.lastUpdated);
                    self.requisition.version(result.data.version);
                    if(result.data.requisitionItems){
                      for(var idx in result.data.requisitionItems){
                        var localItem = self.requisition.findRequisitionItemByOrderIndex(result.data.requisitionItems[idx].orderIndex);
                        localItem.id(result.data.requisitionItems[idx].id);
                        localItem.version(result.data.requisitionItems[idx].version);
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

openboxes.requisition.saveRequisitionToLocal = function(model){
  var data = ko.toJS(model);
  if(!data.id) return null;
  var key = "openboxesRequisition" + data.id;
  openboxes.saveToLocal(key, data);
  return key;
}

openboxes.requisition.getRequisitionFromLocal = function(id){
  if(!id) return null;
  var key = "openboxesRequisition" + id;
  return openboxes.getFromLocal(key);
}

openboxes.saveToLocal = function(name, model){
  if(typeof(Storage) === "undefined") return;
  localStorage[name] = JSON.stringify(model);
};

openboxes.getFromLocal = function(name){
  if(typeof(Storage) !== "undefined" && localStorage[name])
    return JSON.parse(localStorage[name]);
  return null;
};

openboxes.requisition.Requisition.getNewer = function(serverData, localData){
  if(!localData) return serverData;
  if(serverData.version > localData.version) return serverData;
  if(serverData.version < localData.version) return localData;
  if(!serverData.requisitionItems) serverData.requisitionItems = [];
  if(!localData.requisitionItems) localData.requisitionItems = [];
  var serverItemIdVersionMap ={}
  for(var idx in serverData.requisitionItems){
    var serverItem = serverData.requisitionItems[idx];
    serverItemIdVersionMap[serverItem.id] = serverItem.version;
  }
  for(var idx in localData.requisitionItems){
    var localItem = localData.requisitionItems[idx];
    if(localItem.version!=null && localItem.id !=null && serverItemIdVersionMap[localItem.id]!=null ){
      if(localItem.version < serverItemIdVersionMap[localItem.id]){
       return serverData;
      }
    }
  }
  return localData;
};



