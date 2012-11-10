(function(ns){
  ns.Requisition = function(id,originId,dateRequested, requestedDeliveryDate, requestedById, recipientProgram) {
      this.id = id;
      this.origin_id= ko.observable(originId);
      this.dateRequested = ko.observable(dateRequested);
      this.requestedDeliveryDate = ko.observable(requestedDeliveryDate);
      this.requestedBy_id = ko.observable(requestedById);
      this.recipientProgram = ko.observable(recipientProgram);
  }

  ns.RequisitionItem = function(id, productId, quatity, comment, substitutable, recipient) {
      this.id = id;
      this.productId = ko.observable(productId);
      this.quantity =  ko.observable(quantity);
      this.comment = ko.observable( comment);
      this.substitutable =  ko.observable(substitutable);
      this.recipient = ko.observable( recipient);
  }

  ns.RequisitionItemList = function(items) {
      var self = this;
      this.allItems = ko.observableArray(items);
      this.addItem = function () {
         self.allItems.push(new RequisitionItem());
      };
  }

  ns.ViewModel = function(requisition, items) {
      var self = this;
      this.requisition = requisition;

  //    this.requisitionItemsList = new RequisitionItemsList(items);

      this.save = function(formElement) {
          var data = ko.toJS(self.requisition);
          data.requestedDeliveryDate = self.convertDateToServerFormat(data.requestedDeliveryDate);
          data.dateRequested = self.convertDateToServerFormat(data.dateRequested);
          data["origin.id"] = data.origin_id;
          data["requestedBy.id"] = data.requestedBy_id;
          console.log("here is the req: "  + JSON.stringify( data));
          console.log("endpoint is " + formElement.action);
          jQuery.ajax({
              url: formElement.action,
              type: "POST",
              data: data,
              dataType: "json",
              success: function(result) {
                  console.log("result:" + JSON.stringify(result));
                  if(result.success){
                      self.requisition.id = result.id;
                  }
              }
          });
      }

      this.validate = function(){return true;}
      this.convertDateToServerFormat = function(dateString){
         return $.datepicker.formatDate("mm/dd/yy", new Date(dateString));
      }
  }
})( typeof exports === "undefined" ? (this.warehouse || (this.warehouse ={})) : exports);  



