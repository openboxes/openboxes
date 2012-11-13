var ajaxOptions;
jQuery.ajax = function(options){
    ajaxOptions = options;
  }

describe('requisition view model', function(){
  it('be able to add items', function() {
    var requisition = new warehouse.Requisition();
    var viewModel = new warehouse.ViewModel(requisition);
    viewModel.addItem();
    expect(requisition.requisitionItems().length).toBe(1);
    expect(requisition.requisitionItems()[0].orderIndex()).toEqual(0);
    viewModel.addItem();
    expect(requisition.requisitionItems()[1].orderIndex()).toEqual(1);
  });

  it("should validate user input", function() {
    var requisition = new warehouse.Requisition();
    var viewModel = new warehouse.ViewModel(requisition);
    expect(viewModel.validate()).toBe(false);
    requisition.originId(3);
    requisition.requestedById(100);
    requisition.dateRequested('25/12/2012')
    expect(viewModel.validate()).toBe(true) 

    viewModel.addItem();
    expect(viewModel.validate()).toBe(false);
    requisition.requisitionItems()[0].quantity(3000);
    requisition.requisitionItems()[0].productId("prod1");
    expect(viewModel.validate()).toBe(true) 
  });

  it('should save data to server', function(){
    var originId = "2";
    var dateRequested = "11/12/2012";
    var requestedDeliveryDate = "11/13/2012";
    var requestedById = "23";
    var recipientProgram = "test";
    var requisitionItem1 = new warehouse.RequisitionItem(null, "prod1",300, "my comment1", true, "peter", 2);
    var requisitionItem2 = new warehouse.RequisitionItem(null, "prod2",400, "my comment2", false, "tim", 1);
    var requisition = new warehouse.Requisition(null,originId,dateRequested, requestedDeliveryDate, requestedById, recipientProgram, [requisitionItem1, requisitionItem2]);
    var viewModel = new warehouse.ViewModel(requisition);
    var formElement ={
      action:"testAction"
    }
    viewModel.save(formElement);
    var jsonSent = JSON.parse(ajaxOptions.data);
    expect(jsonSent.id).toBeNull();
    expect(jsonSent['requestedBy.id']).toEqual(requestedById);
    expect(jsonSent['origin.id']).toEqual(originId);
    expect(jsonSent.recipientProgram).toEqual(recipientProgram);
    expect(jsonSent.dateRequested).toEqual(dateRequested);
    expect(jsonSent.requestedDeliveryDate).toEqual(requestedDeliveryDate);
    expect(jsonSent.requisitionItems[0]['product.id']).toEqual("prod1");
    expect(jsonSent.requisitionItems[1]['product.id']).toEqual("prod2");
    expect(jsonSent.requisitionItems[0].quantity).toEqual(300);
    expect(jsonSent.requisitionItems[1].quantity).toEqual(400);
    expect(jsonSent.requisitionItems[0].recipient).toEqual('peter');
    expect(jsonSent.requisitionItems[1].recipient).toEqual('tim');
    expect(jsonSent.requisitionItems[0].substitutable).toBe(true);
    expect(jsonSent.requisitionItems[1].substitutable).toBe(false);
   
    var responseData = {success: true, id: "requisition1", 
        version: 0, 
        requisitionItems:[
         {id: "item2", orderIndex: 1},
         {id: "item1", orderIndex: 2}
        ]};
    ajaxOptions.success(responseData); //mimic server call back 
    expect(requisition.id()).toBe("requisition1");
    expect(requisitionItem1.id()).toBe("item1");
    expect(requisitionItem2.id()).toBe("item2");
     
  });
});
