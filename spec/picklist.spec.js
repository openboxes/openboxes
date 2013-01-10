describe("picklist", function(){
   describe("should find newer version between two requisition picklist objects", function(){

     describe("version at picklistlevel always wins", function(){

       it("there is no local data", function(){
        var serverData = {
          version: 5
        };
        expect(openboxes.requisition.Picklist.getNewer(serverData, null)).toBe(serverData);
      });

      it("server data is newer", function(){
        var serverData = {
          version: 5
        };
        var localData = {
          version: 3
        };
        expect(openboxes.requisition.Picklist.getNewer(serverData, localData)).toBe(serverData);
      });

       it("server data with id is newer if local has no id", function(){
        var serverData = {
          id: 1
        };
        var localData = { };
        expect(openboxes.requisition.Picklist.getNewer(serverData, localData)).toBe(serverData);
      });


      
      it("local data is newer", function(){
        var serverData = {
          version: 3
        };
        var localData = {
          version: 5
        };
        expect(openboxes.requisition.Picklist.getNewer(serverData, localData)).toBe(localData);
      });

    });
    describe("same version at requisition level then check version at item level", function(){
     
      it("no items for both then local win", function(){
        var serverData = {
          version: 3
        };
        var localData = {
          version: 3
        };
        expect(openboxes.requisition.Picklist.getNewer(serverData, localData)).toBe(localData);
      });

      it("no item data for both then local win", function(){
        var serverData = {
          version: 3,
          picklistItems:[]
        };
        var localData = {
          version: 3,
           picklistItems:[]
        };
        expect(openboxes.requisition.Picklist.getNewer(serverData, localData)).toBe(localData);
      });
      
      it("version of items is newer from server data", function(){
        var serverData = {
          version: 1,
          picklistItems:[{id:"a", version: 1}, {id:"b", version: 0} ]
        };
        var localData = {
          version: 1,
          picklistItems:[{id:"a", version: 0},  {id:"b", version: 0} ]
        };
        expect(openboxes.requisition.Picklist.getNewer(serverData, localData)).toBe(serverData);
      });
      
      it("version of items is newer from local data", function(){
        var serverData = {
          version: 3,
          picklistItems:[
            {id:"a", version: 5},
            {id:"b", version: 4}
          ]
        };
        var localData = {
          version: 3,
          picklistItems:[
            {id:"a", version: 5},
            {id:"b", version: 5}
          ]
        };
        expect(openboxes.requisition.Picklist.getNewer(serverData, localData)).toBe(localData);
      });

      it("version of items is same then still local wins", function(){
        var serverData = {
          version: 3,
          picklistItems:[
            {id:"a", version: 5},
            {id:"b", version: 4}
          ]
        };
        var localData = {
          version: 3,
          picklistItems:[
            {id:"a", version: 5},
            {id:"b", version: 4}
          ]
        };
        expect(openboxes.requisition.Picklist.getNewer(serverData, localData)).toBe(localData);
      });

      it("items may not have version in local data", function(){
        var serverData = {
          version: 3,
          picklistItems:[
            {id:"a", version: 5},
            {id:"b", version: 4}
          ]
        };
        var localData = {
          version: 3,
          picklistItems:[
            {id:"a", version: 5},
            {quantity: 67},
            {id:"b", version: 3}
          ]
        };
        expect(openboxes.requisition.Picklist.getNewer(serverData, localData)).toBe(serverData);
      });


    });


  });

});
