
var Requisition = function(id,originId,dateRequested, requestedDeliveryDate, requestedById, recipientProgram) {
    this.id = id;
    this.originId= ko.observable(originId);
    this.dateRequested = ko.observable(dateRequested);
    this.requestedDeliveryDate = ko.observable(requestedDeliveryDate);
    this.requestedById = ko.observable(requestedById);
    this.recipientProgram = ko.observable(recipientProgram);


}

var RequisitionItem = function(id, productId, quatity, comment, substitutable, recipient) {
    this.id = id;
    this.productId = ko.observable(productId);
    this.quantity =  ko.observable(quantity);
    this.comment = ko.observable( comment);
    this.substitutable =  ko.observable(substitutable);
    this.recipient = ko.observable( recipient);


}

var RequisitionItemList = function(items) {
    var self = this;
    this.allItems = ko.observableArray(items);
    this.addItem = function () {
       self.allItems.push(new RequisitionItem());
    };
}

var ViewModel = function(requisition, items) {
    var self = this;
    this.requisition = requisition;

    this.requisitionItemsList = new RequisitionItemsList(items);

    this.save = function(formElement) {
        var data = ko.toJS(self.requisition);
        data.requestedDeliveryDate = self.convertDateToServerFormat(data.requestedDeliveryDate);
        data.dateRequested = self.convertDateToServerFormat(data.dateRequested);
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

};


$(function(){


    var today = $.datepicker.formatDate("dd/M/yy", new Date());
    var tomorrow = $.datepicker.formatDate("dd/M/yy", new Date(new Date().getTime() + 24*60*60*1000));
    var requisition = new Requisition("", 0, today, tomorrow, "", "");
    var viewModel = new ViewModel(requisition);
    ko.applyBindings(viewModel); // This makes Knockout get to work

    $(".date-picker").each(function(index, element){

       var item =  $(element);
       var minDate= item.attr("min-date");
       var maxDate = item.attr("max-date");

       item.datepicker({
           minDate: minDate && new Date(minDate),
           maxDate: maxDate && new Date(maxDate), dateFormat:'dd/M/yy',
           buttonImageOnly: true,
           buttonImage: '${request.contextPath}/images/icons/silk/calendar.png'
       });

    });

    $('.date-picker').change(function() {
        var picker = $(this);
        try {
            var d = $.datepicker.parseDate('dd/M/yy', picker.val());
        } catch(err) {
            picker.val('').trigger("change");
        }
    });


});
