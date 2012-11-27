describe("picklistItem", function(){
  it("quantity picked should not greater than quantity ATP", function(){
    var item = new openboxes.requisition.PicklistItem({quantityATP: 4000});
    item.quantity(1000);
    expect(item.quantity()).toEqual(1000);
    item.quantity(4001);
    expect(item.quantity()).toEqual(4000);

  });
});
