if(typeof RequisitionProcessor === "undefined") RequisitionProcessor = {};

RequisitionProcessor.mapping = {
    'requisitionItems': {
        create: function(options) {
            return new RequisitionProcessor.RequisitionItem(options.data);
        }
    }
};

RequisitionProcessor.RequisitionItem = function(data) {
    ko.mapping.fromJS(data, {}, this);

    this.quantityPicked = ko.computed(function() {
        return 0;
    }, this);
    this.quantityRemaining = ko.computed(function() {
        return this.quantity() - this.quantityPicked();
    }, this);
    this.status = ko.computed(function() {
        if(this.quantityPicked() == 0) return "Incomplete";
        if(this.quantityPicked() >= this.quantity()) return "Complete";
        return "PartiallyComplete";
    }, this);
    this.rowIndex = ko.computed(function() {
        return this.orderIndex() + 1;
    }, this);
};