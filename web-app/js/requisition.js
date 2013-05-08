if(typeof openboxes === "undefined") openboxes = {};
if(typeof openboxes.requisition === "undefined") openboxes.requisition = {};
openboxes.requisition.Picklist = function(attrs) {
    var self = this;
    if(!attrs) attrs = {};
    self.id = ko.observable(attrs.id);
    self.version = ko.observable(attrs.version);
    self.requisitionId = ko.observable(attrs.requisitionId);
    var getPickedItems = attrs.getPickedItems;
    self.picklistItems = [];
    self.updatePickedItems = function(){ self.picklistItems = getPickedItems();};
};

openboxes.requisition.PicklistItem = function(attrs){
    var self = this;
    if(!attrs) attrs = {};
    self.id = ko.observable(attrs.id);
    self.version = ko.observable(attrs.version);
    self.requisitionItemId = ko.observable(attrs.requisitionItemId);
    self.inventoryItemId = ko.observable(attrs.inventoryItemId);
    self.lotNumber = ko.observable(attrs.lotNumber);
    self.unitOfMeasure = ko.observable(attrs.unitOfMeasure);
    self.expirationDate = ko.observable(formatDate(attrs.expirationDate));
    self.quantityOnHand = ko.observable(attrs.quantityOnHand);
    self.quantityATP = ko.observable(attrs.quantityATP);
    self.quantity = ko.observable(attrs.quantity || "");
    self.quantity.subscribe(function(newValue){
      if(newValue > self.quantityATP())
        self.quantity(self.quantityATP());
    });

    function formatDate(dateString){
      if(!dateString) return "";
      var date = $.datepicker.parseDate('mm/dd/yy', dateString);
      return $.datepicker.formatDate('dd/M/yy', date);
    }

};
openboxes.requisition.Requisition = function(attrs) {
	console.log("initialize requisition");
	console.log(attrs);
    var self = this;
    if(!attrs) attrs = {};
    self.id = ko.observable(attrs.id);
    self.type = ko.observable(attrs.type);
    self.originId= ko.observable(attrs.originId);
    self.originName = ko.observable(attrs.originName);
    self.dateRequested = ko.observable(attrs.dateRequested);
    self.description = ko.observable(attrs.description);
    self.requestedDeliveryDate = ko.observable(attrs.requestedDeliveryDate);
    self.requestedById = ko.observable(attrs.requestedById);
    self.requestedByName = ko.observable(attrs.requestedByName);
    self.recipientProgram = ko.observable(attrs.recipientProgram);
    self.lastUpdated = ko.observable(attrs.lastUpdated);
    self.status = ko.observable(attrs.status);
    self.version = ko.observable(attrs.version);
    self.requisitionItems = ko.observableArray([]);
    self.name = ko.observable(attrs.name);
    self.getPickedItems = getPickedItems; 
    _.each(attrs.requisitionItems, function(requisitionItemData){
      self.requisitionItems.push(new openboxes.requisition.RequisitionItem(requisitionItemData));
    });

    var picklist = attrs.picklist || {}
    picklist.requisitionId = attrs.id;
    picklist.getPickedItems = self.getPickedItems;
    self.picklist = new openboxes.requisition.Picklist(picklist);
 
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


    //private functions
    function newOrderIndex(){
      var orderIndex = -1;
      for(var idx in self.requisitionItems()){
        if(self.requisitionItems()[idx].orderIndex() > orderIndex) 
          orderIndex = self.requisitionItems()[idx].orderIndex();
      }
      return orderIndex + 1;
    };

    function getPickedItems(){
      var picklistItems = [];
      _.each(self.requisitionItems(), function(requisitionItem){
        var pickedItems = _.filter(requisitionItem.picklistItems(), function(picklistItem){
          return picklistItem.quantity() > 0;
        });
        _.each(pickedItems, function(picklistItem){
          picklistItems.push(picklistItem);
        });
      });
      return picklistItems;
    }
 };


openboxes.requisition.RequisitionItem = function(attrs) {
    var self = this;
    if(!attrs) attrs = {};
    console.log(attrs);
    self.id = ko.observable(attrs.id);
    self.version = ko.observable(attrs.version);
    self.productId = ko.observable(attrs.productId);
    self.productName = ko.observable(attrs.productName);
    self.productPackageId = ko.observable(attrs.productPackageId);
    self.productPackageName = ko.observable(attrs.productPackageName);
    self.productPackageQuantity = ko.observable(attrs.productPackageQuantity);
    self.quantity =  ko.observable(attrs.quantity);
    self.totalQuantity =  ko.observable(attrs.totalQuantity);
    self.comment = ko.observable(attrs.comment);
    self.unitOfMeasure = ko.observable(attrs.unitOfMeasure);
    self.status = ko.observable(attrs.status?attrs.status.name:"Pending");
    //self.substitutable =  ko.observable(attrs.substitutable);
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

    self.totalQuantity = ko.computed(function() {
        return self.quantity() * self.productPackageQuantity();
    }, this);

    self.quantityPicked = ko.computed(function() {
        var sum = 0;
        for(var i in self.picklistItems()) {
            sum += parseInt(self.picklistItems()[i].quantity() || 0);
        }
        return sum;
    }, this);

    self.quantityRemaining = ko.computed(function() {
        var num = this.quantity() - this.quantityPicked();
        return (num > 0) ? num : 0
    }, this);

    //self.status = ko.computed(function() {
    //    if(this.quantityPicked() == 0) return "Incomplete";
    //    if(this.quantityPicked() >= this.quantity()) return "Complete";
    //    return "PartiallyComplete";
    //}, this);
};

openboxes.requisition.ProcessViewModel = function(requisitionData, picklistData, inventoryItemsData) {
    var self = this;

    requisitionData.picklist = picklistData;
    _.each(requisitionData.requisitionItems, function(requisitionItem){
      requisitionItem.picklistItems = createPicklistItems(requisitionItem);
    });
    self.requisition = new openboxes.requisition.Requisition(requisitionData);
   
    self.save = function(formElement) {
        self.requisition.picklist.updatePickedItems();
        var picklist = ko.toJS(self.requisition.picklist);
        picklist["id"] = picklist.id || "";
        picklist["requisition.id"] = picklist.requisitionId;
        _.each(picklist.picklistItems, function(pickedItem){
              pickedItem["id"] = pickedItem.id || "";
              pickedItem["inventoryItem.id"] = pickedItem.inventoryItemId;
              pickedItem["requisitionItem.id"] = pickedItem.requisitionItemId;
              pickedItem["quantity"] = pickedItem.quantity;
              delete pickedItem.version;
        });
        var jsonString = JSON.stringify(picklist);

        //console.log("here are the picklistItems: "  + jsonString);
        //console.log("endpoint is " + formElement.action);
        jQuery.ajax({
            url: formElement.action,
            contentType: 'text/json',
            type: "POST",
            data: jsonString,
            dataType: "json",
            async: false,
            success: function(result) {
                //console.log("result:" + JSON.stringify(result));
                if(result.success){
                  self.requisition.picklist.id(result.data.id);
                  self.requisition.picklist.version(result.data.version);
                  if(result.data.picklistItems) {
                      _.each(result.data.picklistItems, function(picklistItem){
                        var localRequisitionItem = self.requisition.findRequisitionItemById(picklistItem.requisitionItemId);
                        var localItem = localRequisitionItem.findPicklistItemByInventoryItemId(picklistItem.inventoryItemId);
                        localItem.id(picklistItem.id);
                        localItem.version(picklistItem.version);
                      });
                   }
                   if(self.savedCallback) self.savedCallback();
                }
            }
        });
    };

  //private functions
   function createPicklistItems(requisitionItem){
       var inventoryItems = inventoryItemsData[requisitionItem.productId];
       return  _.map(inventoryItems, function(picklistItemData){
        picklistItemData.version = null;
        picklistItemData.requisitionItemId = requisitionItem.id;
        var matchedPicklistItem = _.find(picklistData.picklistItems, function(picklistItem){
          return picklistItem.requisitionItemId == requisitionItem.id && picklistItem.inventoryItemId == picklistItemData.inventoryItemId;
        });
        if(matchedPicklistItem){
          picklistItemData.quantity = matchedPicklistItem.quantity;
          picklistItemData.id = matchedPicklistItem.id;
          picklistItemData.version = matchedPicklistItem.version;
        }
        return picklistItemData;
      });
    }
};

openboxes.requisition.EditRequisitionViewModel = function(requisitionData) {
    var self = this;
    self.requisition = new openboxes.requisition.Requisition(requisitionData);
    self.save = function(formElement) {
        printMessage("in save");
        var jsonString = getJsonDataFromRequisition();
        printMessage("here is the req: "  + jsonString);
        printMessage("endpoint is " + formElement.action);
        try {
            jQuery.ajax({
                url: formElement.action,
                contentType: 'text/json',
                type: "POST",
                data: jsonString,
                async: false,
                dataType: "json",
                success: function(result) {
                    printMessage("success");
                    //console.log("result:" + JSON.stringify(result));
                    if(result.success){
                        self.requisition.id(result.data.id);
                        self.requisition.status(result.data.status);
                        self.requisition.lastUpdated(result.data.lastUpdated);
                        self.requisition.version(result.data.version);
                        if(result.data.requisitionItems){
                          for(var idx in result.data.requisitionItems){
                            var localItem = self.requisition.findRequisitionItemByOrderIndex(result.data.requisitionItems[idx].orderIndex);
                              console.log("idx");
                              console.log(idx);
                              console.log("result.data.requisitionItems[idx].orderIndex");
                              console.log(result.data.requisitionItems[idx].orderIndex);
                              console.log("localItem");
                              console.log(localItem);
                            localItem.id(result.data.requisitionItems[idx].id);
                            localItem.version(result.data.requisitionItems[idx].version);
                          }
                        }
                        if(self.savedCallback) self.savedCallback();
                    }
                    else {
                        var errorMessage = "Please fix the following error(s):\n"
                        console.log("ERRORS");
                        console.log(result.errors.errors);
                        if (result.errors.errors.length > 0) {
                            for (var i=0; i<result.errors.errors.length; i++) {
                                errorMessage += " * " + result.errors.errors[i].message + "\n";
                            }
                            alert(errorMessage);
                        }
                        else if (result.message) {
                            alert("Error:\n" + result.message);
                        }
                        else {
                            alert("Unknown error.  Please try again.")
                        }
                    }
                },
                error: function() {
                    printMessage("failure");
                    alert("There was an error saving requisition items");
                }
            });
        } catch (err) {
            printMessage("here is the err: " + err);
            alert("There was an error saving requisition items " + err);
        }

    };

    //private functions
    function getJsonDataFromRequisition() {
        var data = ko.toJS(self.requisition);
        console.log(data);
        data["origin.id"] = data.originId;
        data["requestedBy.id"] = data.requestedById;
        delete data.version;
        delete data.status;
        delete data.lastUpdated;
        for (var attr in data) {
            if (data[attr] == null) delete data[attr];
        }
        _.each(data.requisitionItems, function (item) {
            // Convert to variable names that will automatically bind on the server
            item["product.id"] = item.productId;
            item["productPackage.id"] = item.productPackageId;

            for (var attr in item) {
                if (item[attr] == null) delete item[attr];
            }
            delete item.version;
        });
        return JSON.stringify(data);
    }
};

openboxes.requisition.saveRequisitionToLocal = function(model){
  var data = ko.toJS(model);
  if(!data.id) return null;
  var key = "openboxesRequisition" + data.id;
  openboxes.saveToLocal(key, data);
  return key;
};

openboxes.requisition.getRequisitionFromLocal = function(id){
  if(!id) return null;
  var key = "openboxesRequisition" + id;
  return openboxes.getFromLocal(key);
};

openboxes.requisition.deleteRequisitionFromLocal = function(id){
  if(!id) return null;
  var key = "openboxesRequisition" + id;
  return openboxes.deleteFromLocal(key);
};

openboxes.requisition.savePicklistToLocal = function(model){
  var data = ko.toJS(model);
  if(!data.requisitionId) return null;
  var key = "openboxesPicklist" + data.requisitionId;
  openboxes.saveToLocal(key, data);
  return key;
};

openboxes.requisition.getPicklistFromLocal = function(id){
  if(!id) return null;
  var key = "openboxesPicklist" + id;
  return openboxes.getFromLocal(key);
};

openboxes.requisition.deletePicklistFromLocal = function(id){
  if(!id) return null;
  var key = "openboxesPicklist" + id;
  return openboxes.deleteFromLocal(key);
};

openboxes.saveToLocal = function(name, model){
  if(typeof(Storage) === "undefined") return;
  localStorage[name] = JSON.stringify(model);
  var manager = JSON.parse(localStorage.openboxesManager || "{}" );
  manager[name] = new Date();
  localStorage.openboxesManager = JSON.stringify(manager);
};

openboxes.getFromLocal = function(name){
  if(typeof(Storage) !== "undefined" && localStorage[name])
    return JSON.parse(localStorage[name]);
  return null;
};

openboxes.deleteFromLocal = function(name){
  if(typeof(Storage) !== "undefined" && localStorage[name])
    delete localStorage[name];
};

openboxes.expireFromLocal = function() {
  if(localStorage.openboxesManager) {
    var manager = JSON.parse(localStorage.openboxesManager);
    for(var prop in manager) {
        var currentDate = new Date();
        var lastAccessDate = new Date(manager[prop]);
        if(currentDate > lastAccessDate.setDate(lastAccessDate.getDate() + 7)) {
            openboxes.deleteFromLocal(prop);
        }
    }
  }
};

openboxes.requisition.Requisition.getNewer = function(serverData, localData){
  localData = null;
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

openboxes.requisition.Picklist.getNewer = function(serverData, localData){
  if(!localData) return serverData;
  if(serverData.id && !localData.id) return serverData;
  if(serverData.version > localData.version) return serverData;
  if(serverData.version < localData.version) return localData;
  if(!serverData.picklistItems) serverData.picklistItems = [];
  if(!localData.picklistItems) localData.picklistItems = [];
  var serverItemIdVersionMap ={}
  for(var idx in serverData.picklistItems){
    var serverItem = serverData.picklistItems[idx];
    serverItemIdVersionMap[serverItem.id] = serverItem.version;
  }
  for(var idx in localData.picklistItems){
    var localItem = localData.picklistItems[idx];
    if(localItem.version!=null && localItem.id !=null && serverItemIdVersionMap[localItem.id]!=null ){
      if(localItem.version < serverItemIdVersionMap[localItem.id]){
       return serverData;
      }
    }
  }
  return localData;
};

window.printMessage = function(message) {
    //var html = $("<p>" + message + "</p>");
    //$("#debug").append(html);
    console.log(message);
};

