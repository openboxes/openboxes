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
     var inventoryItemsData;
   
    beforeEach(function(){
      requisitionData = {
        id:"r1",
        requisitionItems:[
          {id:"requisitionItem1", productId: "prod1"},
          {id:"requisitionItem2", productId: "prod2"},
        ],
     };

       plItem1 = {id:"picklistItem1", requisitionItemId:"requisitionItem1", inventoryItemId:"inventoryItem1", quantity:300};
       plItem2 = {id:"picklistItem2", requisitionItemId:"requisitionItem1", inventoryItemId:"inventoryItem2", quantity:100};
       plItem3 = {id:"picklistItem3", requisitionItemId:"requisitionItem2", inventoryItemId:"inventoryItem3", quantity:50};
       picklistData = {
         id: "picklist1",
         picklistItems:[plItem1, plItem2, plItem3],
       };

       inventoryItem1 =  {inventoryItemId:"inventoryItem1", lotNumber:"batch1", expirationDate: "12/20/2012", quantityOnHand: 300};
       inventoryItem2 =  {inventoryItemId:"inventoryItem2", lotNumber:"batch2", expirationDate: "12/21/2012", quantityOnHand: 400};
       inventoryItem3 =  {inventoryItemId:"inventoryItem3", lotNumber:"batch3", expirationDate: "12/23/2012", quantityOnHand:00};
       inventoryItemsData = {
        "prod1": [ inventoryItem1, inventoryItem2],
        "prod2": [ inventoryItem3 ]
       }
       
    });

    it('should build picklist items from inventory items when there is no picklist items given', function() {  
    
     var  viewModel = new openboxes.requisition.ProcessViewModel(requisitionData, picklistData, inventoryItemsData);

     var requisition = viewModel.requisition;

     expect(requisition).not.toBeNull();
     
     var picklistItem1 = requisition.requisitionItems()[0].picklistItems()[0];
     var picklistItem2 = requisition.requisitionItems()[0].picklistItems()[1];
     var picklistItem3 = requisition.requisitionItems()[1].picklistItems()[0];
     expect(picklistItem1.inventoryItemId()).toEqual(inventoryItem1.inventoryItemId);
     expect(picklistItem1.requisitionItemId()).toEqual("requisitionItem1");
     expect(picklistItem1.lotNumber()).toEqual(inventoryItem1.lotNumber);
     expect(picklistItem1.expirationDate()).toEqual(inventoryItem1.expirationDate);
     expect(picklistItem1.quantityOnHand()).toEqual(inventoryItem1.quantityOnHand);
     expect(picklistItem1.quantityPicked()).toEqual(plItem1.quantity);


     expect(picklistItem2.inventoryItemId()).toEqual(inventoryItem2.inventoryItemId);
     expect(picklistItem2.requisitionItemId()).toEqual("requisitionItem1");
     expect(picklistItem2.lotNumber()).toEqual(inventoryItem2.lotNumber);
     expect(picklistItem2.expirationDate()).toEqual(inventoryItem2.expirationDate);
     expect(picklistItem2.quantityOnHand()).toEqual(inventoryItem2.quantityOnHand);
     expect(picklistItem2.quantityPicked()).toEqual(plItem2.quantity);

     expect(picklistItem3.inventoryItemId()).toEqual(inventoryItem3.inventoryItemId);
     expect(picklistItem3.requisitionItemId()).toEqual("requisitionItem2");
     expect(picklistItem3.lotNumber()).toEqual(inventoryItem3.lotNumber);
     expect(picklistItem3.expirationDate()).toEqual(inventoryItem3.expirationDate);
     expect(picklistItem3.quantityOnHand()).toEqual(inventoryItem3.quantityOnHand);
     expect(picklistItem3.quantityPicked()).toEqual(plItem3.quantity);

     expect(viewModel.picklistId).toEqual(picklistData.id);

    });

  
    describe("should save picklist", function(){
      it("should get picklist items from requisition", function(){});
         //var viewModel = new openboxes.requisition.ProcessViewModel(requisitionData, picklistData, inventoryItemsData);

    });
});
