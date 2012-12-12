var ajaxOptions;
jQuery.ajax = function(options){
    ajaxOptions = options;
};

describe("requisition view model", function(){
  
  it("should save data to server", function(){
    var originId = "2";
    var dateRequested = "11/12/2012";
    var requestedDeliveryDate = "11/13/2012";
    var requestedById = "23";
    var recipientProgram = "test";
    var requisitionItem1 = {productId:"prod1", quantity:300, version:1, comment:"my comment1",
        //substitutable:true,
        recipient: "peter",orderIndex: 2};
    var requisitionItem2 = {productId:"prod2", quantity:400, version:2, comment:"my comment2",
        //substitutable:false,
        recipient: "tim",orderIndex: 1};

    var requisition = {
      originId: originId,
      version: 3,
      lastUpdated: "11/25/2012",
      status: "Created",
      dateRequested: dateRequested, 
      requestedDeliveryDate: requestedDeliveryDate, 
      requestedById: requestedById,
      recipientProgram:recipientProgram,
      requisitionItems: [requisitionItem1, requisitionItem2]};
    var viewModel = new openboxes.requisition.EditRequisitionViewModel(requisition);
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
    //expect(jsonSent.requisitionItems[0].substitutable).toBe(true);
    //expect(jsonSent.requisitionItems[1].substitutable).toBe(false);
    expect(jsonSent.requisitionItems[0].comment).toEqual("my comment1");
    expect(jsonSent.requisitionItems[1].comment).toEqual("my comment2");
    expect(jsonSent.requisitionItems[0].version).not.toBeDefined();
    expect(jsonSent.requisitionItems[1].version).not.toBeDefined();
   

   
    var responseData = {success: true, 
          data:{
            id: "requisition1",
            status: "CREATED",
            lastUpdated: "14/Nov/2012 12:12 PM",
            version: 0, 
            requisitionItems:[
             {id: "item2", orderIndex: 1, version: 1},
             {id: "item1", orderIndex: 2, version: 2}
            ]
          }
        };
    ajaxOptions.success(responseData); //mimic server call back 
    expect(viewModel.requisition.id()).toBe(responseData.data.id);
    expect(viewModel.requisition.status()).toBe(responseData.data.status);
    expect(viewModel.requisition.lastUpdated()).toBe(responseData.data.lastUpdated);
    expect(viewModel.requisition.version()).toBe(responseData.data.version);
    expect(viewModel.requisition.requisitionItems()[0].id()).toBe("item1");
    expect(viewModel.requisition.requisitionItems()[1].id()).toBe("item2");
    expect(viewModel.requisition.requisitionItems()[0].version()).toBe(2);
    expect(viewModel.requisition.requisitionItems()[1].version()).toBe(1);
     
  });

  it("should convert null to empty string when saving data to server", function(){
    var requisitionItem = {productId:"prod1", quantity:300, version:1,
    id:null, comment:null, substitutable:true, 
    recipient: null,orderIndex: 2};
    var requisition = {
      id: null,
      originId: 2,
      version: 3,
      lastUpdated: "11/25/2012",
      status: "Created",
      dateRequested: "11/12/2012", 
      requestedDeliveryDate: "11/13/2012", 
      requestedById: 23,
      recipientProgram: null,
      requisitionItems: [requisitionItem]};
    var viewModel = new openboxes.requisition.EditRequisitionViewModel(requisition);
    var formElement ={
      action:"testAction"
    }
    viewModel.save(formElement);
    var jsonSent = JSON.parse(ajaxOptions.data);
    expect(jsonSent['id']).toBeUndefined();
    expect(jsonSent['recipientProgram']).toBeUndefined();
    expect(jsonSent.requisitionItems[0].recipient).toBeUndefined();
    expect(jsonSent.requisitionItems[0].comment).toBeUndefined();
    expect(jsonSent.requisitionItems[0].id).toBeUndefined();
   

     
  });


});
