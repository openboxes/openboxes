var ajaxOptions;
jQuery.ajax = function(options){
    ajaxOptions = options;
  }

describe('requisition view model', function(){
  it('be able to add items', function() {
    var requisition = new warehouse.Requisition({});
    var viewModel = new warehouse.ViewModel(requisition);
    viewModel.addItem();
    expect(requisition.requisitionItems().length).toBe(1);
    expect(requisition.requisitionItems()[0].orderIndex()).toEqual(0);
    viewModel.addItem();
    expect(requisition.requisitionItems()[1].orderIndex()).toEqual(1);
  });

  it("should add one item if there is no item in existing requisition", function(){
    var requisition = new warehouse.Requisition({id:"abc"});
    var viewModel = new warehouse.ViewModel(requisition);
    expect(requisition.requisitionItems().length).toBe(1);
  });

  it('be able to remove items but alway add one when items are empty', function() {
    var requisition = new warehouse.Requisition({});
    var viewModel = new warehouse.ViewModel(requisition);
    viewModel.addItem();
    viewModel.addItem();
   
    var item1 = requisition.requisitionItems()[0];
    var item2 = requisition.requisitionItems()[1];
    viewModel.removeItem(item1);
    expect(requisition.requisitionItems().length).toEqual(1);
    viewModel.removeItem(item2);
    expect(requisition.requisitionItems().length).toEqual(1); //should add new one when items is empty
  });

  it("should build requisition item models when build requisition model", function(){
    var data = {
      id: "abc",
      requisitionItems: [
        {id: "item1"},
        {id: "item2"}
      ]
    };
    var requisition = new warehouse.Requisition(data);
    expect(requisition.requisitionItems()[0].id()).toBe("item1");
    expect(requisition.requisitionItems()[1].id()).toBe("item2");
  });

  
  it('should save data to server', function(){
    var originId = "2";
    var dateRequested = "11/12/2012";
    var requestedDeliveryDate = "11/13/2012";
    var requestedById = "23";
    var recipientProgram = "test";
    var requisitionItem1 = {productId:"prod1", quantity:300, 
    comment:"my comment1", substitutable:true, recipient: "peter",orderIndex: 2};
    var requisitionItem2 = {productId:"prod2", quantity:400, 
    comment:"my comment2", substitutable:false, recipient: "tim",orderIndex: 1};
    var requisition = new warehouse.Requisition({
      originId: originId,
      version: 3,
      lastUpdated: "11/25/2012",
      status: "Created",
      dateRequested: dateRequested, 
      requestedDeliveryDate: requestedDeliveryDate, 
      requestedById: requestedById,
      recipientProgram:recipientProgram,
      requisitionItems: [requisitionItem1, requisitionItem2]});
    var viewModel = new warehouse.ViewModel(requisition);
    var formElement ={
      action:"testAction"
    }
    viewModel.save(formElement);
    var jsonSent = JSON.parse(ajaxOptions.data);
    expect(jsonSent['requestedBy.id']).toEqual(requestedById);
    expect(jsonSent['origin.id']).toEqual(originId);
    expect(jsonSent.version).not.toBeDefined();
    expect(jsonSent.status).not.toBeDefined();
    expect(jsonSent.lastUpdated).not.toBeDefined();
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
    expect(jsonSent.requisitionItems[0].comment).toEqual("my comment1");
    expect(jsonSent.requisitionItems[1].comment).toEqual("my comment2");
   

   
    var responseData = {success: true, 
        id: "requisition1",
        status: "CREATED",
        lastUpdated: "14/Nov/2012 12:12 PM",
        version: 0, 
        requisitionItems:[
         {id: "item2", orderIndex: 1},
         {id: "item1", orderIndex: 2}
        ]};
    ajaxOptions.success(responseData); //mimic server call back 
    expect(requisition.id()).toBe(responseData.id);
    expect(requisition.status()).toBe(responseData.status);
    expect(requisition.lastUpdated()).toBe(responseData.lastUpdated);
    expect(requisition.version()).toBe(responseData.version);
    expect(requisition.requisitionItems()[0].id()).toBe("item1");
    expect(requisition.requisitionItems()[1].id()).toBe("item2");
     
  });
});
