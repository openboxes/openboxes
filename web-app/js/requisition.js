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
    
    _.each(attrs.requisitionItems, function(requisitionItemData){
      self.requisitionItems.push(new openboxes.requisition.RequisitionItem(requisitionItemData));

    });

    self.findRequisitionItemByOrderIndex = function(orderIndex){
      return _.find(self.requisitionItems(), function(requisitionItem){
        return requisitionItem.orderIndex() == orderIndex;
      });
    };

    self.findRequisitionItemById = function(id){
      return _.find(self.requisitionItems(), function(requisitionItem){
        return requisitionItem.id() == id;
      });
    };

   
    self.addItem = function () {
       self.requisitionItems.push(
        new openboxes.requisition.RequisitionItem({orderIndex: newOrderIndex()})
       );
    };

    self.removeItem = function(item){
       self.requisitionItems.remove(item);
       if(self.requisitionItems().length == 0)
         self.addItem();

    };

    if(self.requisitionItems().length == 0 && self.id()){
            self.addItem();
    } 

    self.id.subscribe(function(newValue) {
        if(newValue){
          self.addItem();
        }
    });

    //private functions
    function newOrderIndex(){
      var orderIndex = -1;
      for(var idx in self.requisitionItems()){
        if(self.requisitionItems()[idx].orderIndex() > orderIndex) 
          orderIndex = self.requisitionItems()[idx].orderIndex();
      }
      return orderIndex + 1;
    };


    
 };

openboxes.requisition.Picklist = function(attrs) {
    var self = this;
    if(!attrs) attrs = {};
    self.id = ko.observable(attrs.id);
    self.version = ko.observable(attrs.version);
    self.requisitionId = ko.observable(attrs.requisitionId);
};

openboxes.requisition.PicklistItem = function(attrs){
    var self = this;
    if(!attrs) attrs = {};
    self.id = ko.observable(attrs.id);
    self.version = ko.observable(attrs.version);
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
    
    _.each(attrs.picklistItems, function(picklistItem){
      self.picklistItems.push(new openboxes.requisition.PicklistItem(picklistItem));
    });

    self.findPicklistItemByInventoryItemId = function(inventoryItemId){
      return _.find(self.picklistItems(), function(picklistItem){
        return picklistItem.inventoryItemId() == inventoryItemId;
      });
    };

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
    _.each(requisitionData.requisitionItems, function(requisitionItem){
      requisitionItem.picklistItems = getPicklistItems(requisitionItem);
    });

    console.log(requisitionData);

    self.requisition = new openboxes.requisition.Requisition(requisitionData);
    self.picklist = new openboxes.requisition.Picklist(picklistData);

   
    self.save = function(formElement) {
        var result = {
          id: self.picklist.id() || "",
          "requisition.id": self.requisition.id(),
          picklistItems:[]
          };
        _.each(ko.toJS(self.requisition).requisitionItems, function(item){
            _.each(item.picklistItems, function(pl) {
              if(pl.quantityPicked != 0) {
                pl["id"] = pl.id || "";
                pl["inventoryItem.id"] = pl.inventoryItemId;
                pl["requisitionItem.id"] = pl.requisitionItemId;
                pl["quantity"] = pl.quantityPicked;
                delete pl.version;
                result.picklistItems.push(pl);
              }
            });
        });
        var jsonString = JSON.stringify(result);
        console.log("here are the picklistItems: "  + jsonString);
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
                  self.picklist.id(result.data.id);
                  self.picklist.version(result.data.version);
                  if(result.data.picklistItems) {
                      _.each(result.data.picklistItems, function(picklistItem){
                        var localRequisitionItem = self.requisition.findRequisitionItemById(picklistItem.requisitionItemId);
                        console.log("********" + localRequisitionItem);
                        var localItem = localRequisitionItem.findPicklistItemByInventoryItemId(picklistItem.inventoryItemId);
                        localItem.id(picklistItem.id);
                        localItem.version(picklistItem.version);
                      });
                   }
                }
            }
        });
    };

  //private functions
   function getPicklistItems(requisitionItem){
       var inventoryItems = inventoryItemsData[requisitionItem.productId];
       return  _.map(inventoryItems, function(picklistItemData){
        picklistItemData.requisitionItemId = requisitionItem.id;
        var matchedPicklistItem = _.find(picklistData.picklistItems, function(picklistItem){
          return picklistItem.requisitionItemId == requisitionItem.id && picklistItem.inventoryItemId == picklistItemData.inventoryItemId;
        });
        if(matchedPicklistItem){
          picklistItemData.quantityPicked = matchedPicklistItem.quantity;
          picklistItemData.id = matchedPicklistItem.id;
          picklistItemData.version = matchedPicklistItem.version;
        }
        return picklistItemData;
      });
    }
};

openboxes.requisition.ViewModel = function(requisition) {
    var self = this;
    self.requisition = requisition;
    
    self.processRequisition = function () {
        window.location = '../process/' + self.requisition.id();
    };
    
    self.deleteRequisition = function () {
        window.location = '../delete/' + self.requisition.id();
    };

    self.save = function(formElement) {
        var redirect = true;
        if(self.requisition.id()) {
            redirect = false;
        }
        var data = ko.toJS(self.requisition);
        data["origin.id"] = data.originId;
        data["requestedBy.id"] = data.requestedById;
        delete data.version;
        delete data.status;
        delete data.lastUpdated;
        _.each(data.requisitionItems, function(item){
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
                    if(redirect){
                        window.location = "./edit/" + self.requisition.id();
                    }
                }
            }
        });

    };   
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



