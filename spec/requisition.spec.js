describe("requisition model", function(){
 
 it("should get new orderIndex of requisitionItem", function(){
    var requisition = new warehouse.Requisition();
    expect(requisition.newOrderIndex()).toEqual(0);
  });


  it("should build requisition item models when build requisition model", function(){
    var data = {
      id: "abc",
      requisitionItems: [
        {id: "item1", orderIndex: 3},
        {id: "item2", orderIndex: 2}
      ]
    };
    var requisition = new warehouse.Requisition(data);
    expect(requisition.requisitionItems()[0].id()).toBe("item1");
    expect(requisition.requisitionItems()[0].orderIndex()).toBe(3);
    expect(requisition.requisitionItems()[1].id()).toBe("item2");
    expect(requisition.requisitionItems()[1].orderIndex()).toBe(2);
    expect(requisition.newOrderIndex()).toBe(4);
  });

  describe("should find newer version between two requisition json objects", function(){

     describe("version at requisition level always wins", function(){

       it("there is no local data", function(){
        var sererData = {
          version: 5
        };
        expect(warehouse.Requisition.getNewer(sererData, null)).toBe(sererData);
      });

      it("server data is newer", function(){
        var sererData = {
          version: 5
        };
        var localData = {
          version: 3
        };
        expect(warehouse.Requisition.getNewer(sererData, localData)).toBe(sererData);
      });
      
      it("local data is newer", function(){
        var sererData = {
          version: 3
        };
        var localData = {
          version: 5
        };
        expect(warehouse.Requisition.getNewer(sererData, localData)).toBe(localData);
      });

    });

    describe("same version at requisition level then check version at item level", function(){
     
      it("no items for both then local win", function(){
        var sererData = {
          version: 3
        };
        var localData = {
          version: 3
        };
        expect(warehouse.Requisition.getNewer(sererData, localData)).toBe(localData);
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
        expect(warehouse.Requisition.getNewer(sererData, localData)).toBe(localData);
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
        expect(warehouse.Requisition.getNewer(sererData, localData)).toBe(sererData);
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
        expect(warehouse.Requisition.getNewer(sererData, localData)).toBe(localData);
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
        expect(warehouse.Requisition.getNewer(sererData, localData)).toBe(localData);
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
        expect(warehouse.Requisition.getNewer(sererData, localData)).toBe(sererData);
      });


    });


  });


});

