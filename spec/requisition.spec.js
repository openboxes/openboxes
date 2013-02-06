describe("requisition model", function(){
  it("be able to add items", function() {
    var requisition = new openboxes.requisition.Requisition({});
    requisition.addItem();
    expect(requisition.requisitionItems().length).toBe(1);
    expect(requisition.requisitionItems()[0].orderIndex()).toEqual(0);
    requisition.addItem();
    expect(requisition.requisitionItems()[1].orderIndex()).toEqual(1);
  });

  it("should add one item if there is no item in existing requisition", function(){
    var requisition = new openboxes.requisition.Requisition({id:"abc"});
    expect(requisition.requisitionItems().length).toBe(1);
  });

  it('be able to remove items but alway add one when items are empty', function() {
    var requisition = new openboxes.requisition.Requisition();
    requisition.addItem();
    requisition.addItem();
   
    var item1 = requisition.requisitionItems()[0];
    var item2 = requisition.requisitionItems()[1];
    requisition.removeItem(item1);
    expect(requisition.requisitionItems().length).toEqual(1);
    requisition.removeItem(item2);
    expect(requisition.requisitionItems().length).toEqual(1); //should add new one when items is empty
  });
 


  it("should build requisition items and picklist items when build requisition", function(){
    var pickItem1 ={quantity: 15, inventoryItemId: "ii1", requisitionItemIs:"item1"};
    var pickItem2 ={quantity: 0, inventoryItemId: "ii2", requisitionItemIs:"item1"};
    var pickItem3 ={quantity: 0, inventoryItemId: "ii3", requisitionItemIs:"item2"};
    var pickItem4 ={quantity: 30, inventoryItemId: "ii4", requisitionItemIs:"item2"};
    var data = {
      id: "abc",
      picklist: {id:"picklist1"},
      requisitionItems: [
        {id: "item1", orderIndex: 3, picklistItems: [pickItem1, pickItem2], quantity: 5000},
        {id: "item2", orderIndex: 2, picklistItems: [pickItem3, pickItem4], quantity: 4500}
      ]
    };
    var requisition = new openboxes.requisition.Requisition(data);
    expect(requisition.requisitionItems()[0].id()).toBe("item1");
    expect(requisition.requisitionItems()[0].picklistItems()[0].quantity()).toBe(15);
    expect(requisition.requisitionItems()[0].picklistItems()[0].inventoryItemId()).toBe("ii1");
    expect(requisition.requisitionItems()[0].orderIndex()).toBe(3);
    expect(requisition.requisitionItems()[1].id()).toBe("item2");
    expect(requisition.requisitionItems()[1].orderIndex()).toBe(2);
    expect(requisition.picklist.id()).toEqual("picklist1");
    requisition.picklist.updatePickedItems();
    var pickedItems = requisition.picklist.picklistItems;
    expect(pickedItems.length).toBe(2);
    expect(pickedItems[0].inventoryItemId()).toEqual("ii1");
    expect(pickedItems[0].quantity()).toEqual(15);
    expect(pickedItems[1].inventoryItemId()).toEqual("ii4");
  });

  describe("should find newer version between two requisition json objects", function(){
	  /*
     describe("version at requisition level always wins", function(){

       it("there is no local data", function(){
        var sererData = {
          version: 5
        };
        expect(openboxes.requisition.Requisition.getNewer(sererData, null)).toBe(sererData);
      });

      it("server data is newer", function(){
        var sererData = {
          version: 5
        };
        var localData = {
          version: 3
        };
        expect(openboxes.requisition.Requisition.getNewer(sererData, localData)).toBe(sererData);
      });
      
      it("local data is newer", function(){
        var sererData = {
          version: 3
        };
        var localData = {
          version: 5
        };
        expect(openboxes.requisition.Requisition.getNewer(sererData, localData)).toBe(localData);
      });

    });*/
	  
	 /*
    describe("same version at requisition level then check version at item level", function(){
     
      it("no items for both then local win", function(){
        var sererData = {
          version: 3
        };
        var localData = {
          version: 3
        };
        expect(openboxes.requisition.Requisition.getNewer(sererData, localData)).toBe(localData);
      });

      it("no item data for both then local win", function(){
        var sererData = {
          version: 3,
          requisitionItems:[]
        };
        var localData = {
          version: 3,
           requisitionItems:[]
        };
        expect(openboxes.requisition.Requisition.getNewer(sererData, localData)).toBe(localData);
      });
      
      it("version of items is newer from server data", function(){
        var sererData = {
          version: 1,
          requisitionItems:[{id:"a", version: 1}, {id:"b", version: 0} ]
        };
        var localData = {
          version: 1,
          requisitionItems:[{id:"a", version: 0},  {id:"b", version: 0} ]
        };
        expect(openboxes.requisition.Requisition.getNewer(sererData, localData)).toBe(sererData);
      });
      
      it("version of items is newer from local data", function(){
        var sererData = {
          version: 3,
          requisitionItems:[
            {id:"a", version: 5},
            {id:"b", version: 4}
          ]
        };
        var localData = {
          version: 3,
          requisitionItems:[
            {id:"a", version: 5},
            {id:"b", version: 5}
          ]
        };
        expect(openboxes.requisition.Requisition.getNewer(sererData, localData)).toBe(localData);
      });

      it("version of items is same then still local wins", function(){
        var sererData = {
          version: 3,
          requisitionItems:[
            {id:"a", version: 5},
            {id:"b", version: 4}
          ]
        };
        var localData = {
          version: 3,
          requisitionItems:[
            {id:"a", version: 5},
            {id:"b", version: 4}
          ]
        };
        expect(openboxes.requisition.Requisition.getNewer(sererData, localData)).toBe(localData);
      });

      it("items may not have version in local data", function(){
        var sererData = {
          version: 3,
          requisitionItems:[
            {id:"a", version: 5},
            {id:"b", version: 4}
          ]
        };
        var localData = {
          version: 3,
          requisitionItems:[
            {id:"a", version: 5},
            {quantity: 67},
            {id:"b", version: 3}
          ]
        };
        expect(openboxes.requisition.Requisition.getNewer(sererData, localData)).toBe(sererData);
      });


    });
    */


  });


});

