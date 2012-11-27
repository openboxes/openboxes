var ajaxOptions;
jQuery.ajax = function(options){
    ajaxOptions = options;
};
describe('Requisition Processor View Model', function(){
     var requisitionData;
     var plItem1;
     var plItem2;
     var plItem3;
     var picklistData;
     var inventoryItem1;
     var inventoryItem2;
     var inventoryItem3;
     var inventoryItem4;

     var inventoryItemsData;
   
    beforeEach(function(){
      requisitionData = {
        id:"r1",
        requisitionItems:[
          {id:"requisitionItem1", productId: "prod1"},
          {id:"requisitionItem2", productId: "prod2"},
        ],
     };

       plItem1 = {id:"picklistItem1", requisitionItemId:"requisitionItem1", inventoryItemId:"inventoryItem1", quantity:300, version:2};
       plItem2 = {id:"picklistItem2", requisitionItemId:"requisitionItem1", inventoryItemId:"inventoryItem2", quantity:100, version:3};
       plItem3 = {id:"picklistItem3", requisitionItemId:"requisitionItem2", inventoryItemId:"inventoryItem3", quantity:50, version:4};
       picklistData = {
         id: "picklist1",
         version: 1,
         picklistItems:[plItem1, plItem2, plItem3],
       };

       inventoryItem1 =  {inventoryItemId:"inventoryItem1", lotNumber:"batch1", expirationDate: "12/20/2012", quantityOnHand: 300, version: 33};
       inventoryItem2 =  {inventoryItemId:"inventoryItem2", lotNumber:"batch2", expirationDate: "12/21/2012", quantityOnHand: 400, version: 44};
       inventoryItem3 =  {inventoryItemId:"inventoryItem3", lotNumber:"batch3", expirationDate: "12/22/2012", quantityOnHand:00, version: 55};
       inventoryItem4 =  {inventoryItemId:"inventoryItem4", lotNumber:"batch4", expirationDate: "12/23/2012", quantityOnHand:00, version: 66};
       inventoryItemsData = {
        "prod1": [ inventoryItem1, inventoryItem2],
        "prod2": [ inventoryItem3, inventoryItem4]
       }
       
    });

    it('should build picklist items under requisition items from given inventory items and picklist items', function() {  
    
     var  viewModel = new openboxes.requisition.ProcessViewModel(requisitionData, picklistData, inventoryItemsData);

     var requisition = viewModel.requisition;

     expect(requisition).not.toBeNull();
     
     var picklistItem1 = requisition.requisitionItems()[0].picklistItems()[0];
     var picklistItem2 = requisition.requisitionItems()[0].picklistItems()[1];
     var picklistItem3 = requisition.requisitionItems()[1].picklistItems()[0];
     var picklistItem4 = requisition.requisitionItems()[1].picklistItems()[1];
     expect(picklistItem1.inventoryItemId()).toEqual(inventoryItem1.inventoryItemId);
     expect(picklistItem1.requisitionItemId()).toEqual("requisitionItem1");
     expect(picklistItem1.id()).toEqual(plItem1.id);
     expect(picklistItem1.expirationDate()).toEqual("20/Dec/2012");
     expect(picklistItem1.version()).toEqual(plItem1.version);
     expect(picklistItem1.lotNumber()).toEqual(inventoryItem1.lotNumber);
     expect(picklistItem1.quantityOnHand()).toEqual(inventoryItem1.quantityOnHand);
     expect(picklistItem1.quantity()).toEqual(plItem1.quantity);


     expect(picklistItem2.inventoryItemId()).toEqual(inventoryItem2.inventoryItemId);
     expect(picklistItem2.requisitionItemId()).toEqual("requisitionItem1");
     expect(picklistItem2.id()).toEqual(plItem2.id);
     expect(picklistItem2.version()).toEqual(plItem2.version);
     expect(picklistItem2.lotNumber()).toEqual(inventoryItem2.lotNumber);
     expect(picklistItem2.expirationDate()).toEqual("21/Dec/2012");
     expect(picklistItem2.quantityOnHand()).toEqual(inventoryItem2.quantityOnHand);
     expect(picklistItem2.quantity()).toEqual(plItem2.quantity);

     expect(picklistItem3.inventoryItemId()).toEqual(inventoryItem3.inventoryItemId);
     expect(picklistItem3.requisitionItemId()).toEqual("requisitionItem2");
     expect(picklistItem3.id()).toEqual(plItem3.id);
     expect(picklistItem3.version()).toEqual(plItem3.version);
     expect(picklistItem3.lotNumber()).toEqual(inventoryItem3.lotNumber);
     expect(picklistItem3.expirationDate()).toEqual("22/Dec/2012");
     expect(picklistItem3.quantityOnHand()).toEqual(inventoryItem3.quantityOnHand);
     expect(picklistItem3.quantity()).toEqual(plItem3.quantity);

     expect(picklistItem4.inventoryItemId()).toEqual(inventoryItem4.inventoryItemId);
     expect(picklistItem4.requisitionItemId()).toEqual("requisitionItem2");
     expect(picklistItem4.id()).toBeUndefined();
     expect(picklistItem4.version()).toBeNull();
     expect(picklistItem4.lotNumber()).toEqual(inventoryItem4.lotNumber);
     expect(picklistItem4.expirationDate()).toEqual("23/Dec/2012");
     expect(picklistItem4.quantityOnHand()).toEqual(inventoryItem4.quantityOnHand);
     expect(picklistItem4.quantity()).toEqual("");


     expect(viewModel.requisition.picklist.id()).toEqual(picklistData.id);
     expect(viewModel.requisition.picklist.version()).toEqual(picklistData.version);

    });

    describe("should save picklist", function(){
       it("should save new pick list", function(){
         var viewModel = new openboxes.requisition.ProcessViewModel(requisitionData, {}, inventoryItemsData);
         var form = {action:"url"};
         viewModel.requisition.requisitionItems()[0].picklistItems()[0].quantity(100);
         viewModel.requisition.requisitionItems()[0].picklistItems()[1].quantity(0);
         viewModel.requisition.requisitionItems()[1].picklistItems()[0].quantity(500);
         viewModel.save(form);
         var dataSent = JSON.parse(ajaxOptions.data);
         expect(dataSent["id"]).toEqual("");
         expect(dataSent["requisition.id"]).toEqual(requisitionData.id);
         expect(dataSent["picklistItems"].length).toEqual(2);
         expect(dataSent["picklistItems"][0].id).toEqual("");
         expect(dataSent["picklistItems"][0]["requisitionItem.id"]).toEqual("requisitionItem1");
         expect(dataSent["picklistItems"][0]["inventoryItem.id"]).toEqual(inventoryItem1.inventoryItemId);
         expect(dataSent["picklistItems"][0]["quantity"]).toEqual(100);

         expect(dataSent["picklistItems"][1].id).toEqual("");
         expect(dataSent["picklistItems"][1]["requisitionItem.id"]).toEqual("requisitionItem2");
         expect(dataSent["picklistItems"][1]["inventoryItem.id"]).toEqual(inventoryItem3.inventoryItemId);
         expect(dataSent["picklistItems"][1]["quantity"]).toEqual(500);

         var response = {success: true, data: {id: "picklist1", version:10,
           picklistItems:[
             {id: "pi1", version: 3, requisitionItemId: "requisitionItem1", inventoryItemId:"inventoryItem1" },
             {id: "pi2", version:5, requisitionItemId: "requisitionItem2", inventoryItemId:"inventoryItem3" }
             ]
         }};
         ajaxOptions.success(response);

         expect(viewModel.requisition.picklist.id()).toEqual("picklist1");
         expect(viewModel.requisition.picklist.version()).toEqual(10);
         expect( viewModel.requisition.requisitionItems()[0].picklistItems()[0].id()).toEqual("pi1");
         expect( viewModel.requisition.requisitionItems()[1].picklistItems()[0].id()).toEqual("pi2");
         expect( viewModel.requisition.requisitionItems()[0].picklistItems()[0].version()).toEqual(3);
         expect( viewModel.requisition.requisitionItems()[1].picklistItems()[0].version()).toEqual(5);




    });

      it("should update pick list", function(){
         var viewModel = new openboxes.requisition.ProcessViewModel(requisitionData, picklistData, inventoryItemsData);
         var form = {action:"url"};
         viewModel.requisition.requisitionItems()[0].picklistItems()[0].quantity(100);
         viewModel.requisition.requisitionItems()[0].picklistItems()[1].quantity(0);
         viewModel.requisition.requisitionItems()[1].picklistItems()[0].quantity(500);
         viewModel.requisition.requisitionItems()[0].picklistItems()[0].version(5);

         viewModel.save(form);
         var dataSent = JSON.parse(ajaxOptions.data);
         expect(dataSent["id"]).toEqual(picklistData.id);
         expect(dataSent["picklistItems"].length).toEqual(2);
         expect(dataSent["picklistItems"][0].id).toEqual(plItem1.id);
         expect(dataSent["picklistItems"][0].version).toEqual(undefined);
         expect(dataSent["picklistItems"][0]["requisitionItem.id"]).toEqual(plItem1.requisitionItemId);
         expect(dataSent["picklistItems"][0]["inventoryItem.id"]).toEqual(inventoryItem1.inventoryItemId);
         expect(dataSent["picklistItems"][0]["quantity"]).toEqual(100);

         expect(dataSent["picklistItems"][1].id).toEqual(plItem3.id);
         expect(dataSent["picklistItems"][1]["requisitionItem.id"]).toEqual(plItem3.requisitionItemId);
         expect(dataSent["picklistItems"][1]["inventoryItem.id"]).toEqual(inventoryItem3.inventoryItemId);
         expect(dataSent["picklistItems"][1]["quantity"]).toEqual(500);
    });
  });
});
