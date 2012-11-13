if(typeof warehouse === "undefined") warehouse = {};
warehouse.Requisition = function(id,originId,dateRequested, requestedDeliveryDate, requestedById, recipientProgram, items) {
    var self = this;
    self.id = ko.observable(id);
    self.originId= ko.observable(originId);
    self.dateRequested = ko.observable(dateRequested);
    self.requestedDeliveryDate = ko.observable(requestedDeliveryDate);
    self.requestedById = ko.observable(requestedById);
    self.recipientProgram = ko.observable(recipientProgram);
    self.requisitionItems = ko.observableArray(items || []);
    self.findRequisitionItemByOrderIndex = function(orderIndex){
      for(var idx in self.requisitionItems()){
        if(self.requisitionItems()[idx].orderIndex() == orderIndex) 
          return self.requisitionItems()[idx];
      }
    };
    self.validateItems = function(){
      for(var idx in self.requisitionItems()){
        if(!self.requisitionItems()[idx].validate())
          return false;
      }
      return true;
    };
}

warehouse.RequisitionItem = function(id, productId, quantity, comment, substitutable, recipient, orderIndex) {
    var self = this;
    self.id = ko.observable(id);
    self.productId = ko.observable(productId);
    self.quantity =  ko.observable(quantity);
    self.comment = ko.observable( comment);
    self.substitutable =  ko.observable(substitutable);
    self.recipient = ko.observable(recipient);
    self.orderIndex = ko.observable(orderIndex);
    self.validate = function(){
      if(self.productId() && self.quantity())
        return true;
      else
        return false;
    }
}

warehouse.ViewModel = function(requisition) {
    var self = this;
    self.requisition = requisition;
    self.maxOrderIndex = 0;
    self.addItem = function () {
       self.requisition.requisitionItems.push(new warehouse.RequisitionItem(null, null, null, null, null, null, self.maxOrderIndex));
       self.maxOrderIndex += 1;
    };
    self.save = function(formElement) {
        var data = ko.toJS(self.requisition);
        data["origin.id"] = data.originId;
        data["requestedBy.id"] = data.requestedById;
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
                    if(result.requisitionItems){
                      for(var idx in result.requisitionItems){
                        var localItem = self.requisition.findRequisitionItemByOrderIndex(result.requisitionItems[idx].orderIndex);
                        localItem.id(result.requisitionItems[idx].id);
                      }
                    }
                }
            }
        });
    }

    self.validate = function(){
      if(self.requisition.originId() 
        && self.requisition.requestedById() 
        && self.requisition.dateRequested()
        && self.requisition.validateItems())
        return true;
      else
        return false;
    }
}



