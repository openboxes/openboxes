describe("requisitionItem",function(){
    it("should get quantity picked, remaining and status from requisition item", function(){
      var requisitionItem = new openboxes.requisition.RequisitionItem({id: "1234", quantity:5000});
      //expect(requisitionItem.status()).toBe("Incomplete");
      var picklistItem1 = new openboxes.requisition.PicklistItem({
        quantity: 1000,
        quantityOnHand: 2000
      });
      requisitionItem.picklistItems.push(picklistItem1);

      expect(requisitionItem.quantityPicked()).toBe(1000);
      expect(requisitionItem.quantityRemaining()).toBe(4000);
      //expect(requisitionItem.status()).toBe("PartiallyComplete");

      var picklistItem2 = new openboxes.requisition.PicklistItem({
        quantity: 4000,
        quantityOnHand: 4000
      });
      requisitionItem.picklistItems.push(picklistItem2);
      expect(requisitionItem.quantityPicked()).toBe(5000);
      expect(requisitionItem.quantityRemaining()).toBe(0);
      //expect(requisitionItem.status()).toBe("Complete");
    });

});
