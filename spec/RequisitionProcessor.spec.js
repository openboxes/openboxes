describe('Requisition Processor View Model', function(){
    it('should build picklist items from inventory items when there is no picklist items given', function() {
     
     var requisitionData = {
        id:"r1",
        requisitionItems:[
          {id:"requisitionItem1", productId: "prod1"},
          {id:"requisitionItem2", productId: "prod2"},
        ],
     };
     var picklistData = {
       id: "p1",
       picklistItems:[]
     };

     var inventoryItem1 =  {inventoryItemId:"inventoryItem1", lotNumber:"batch1", expirationDate: "12/20/2012", quantityOnHand: 300};
     var inventoryItem2 =  {inventoryItemId:"inventoryItem2", lotNumber:"batch2", expirationDate: "12/21/2012", quantityOnHand: 400};
     var inventoryItem3 =  {inventoryItemId:"inventoryItem3", lotNumber:"batch3", expirationDate: "12/23/2012", quantityOnHand:00};
     var inventoryItemsData = {
      "prod1": [ inventoryItem1, inventoryItem2],
      "prod2": [ inventoryItem3 ]
     }

     var viewModel = new openboxes.requisition.ProcessViewModel(requisitionData, picklistData, inventoryItemsData);

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

     expect(picklistItem2.inventoryItemId()).toEqual(inventoryItem2.inventoryItemId);
     expect(picklistItem2.requisitionItemId()).toEqual("requisitionItem1");
     expect(picklistItem2.lotNumber()).toEqual(inventoryItem2.lotNumber);
     expect(picklistItem2.expirationDate()).toEqual(inventoryItem2.expirationDate);
     expect(picklistItem2.quantityOnHand()).toEqual(inventoryItem2.quantityOnHand);

     expect(picklistItem3.inventoryItemId()).toEqual(inventoryItem3.inventoryItemId);
     expect(picklistItem3.requisitionItemId()).toEqual("requisitionItem2");
     expect(picklistItem3.lotNumber()).toEqual(inventoryItem3.lotNumber);
     expect(picklistItem3.expirationDate()).toEqual(inventoryItem3.expirationDate);
     expect(picklistItem3.quantityOnHand()).toEqual(inventoryItem3.quantityOnHand);



    });
});
