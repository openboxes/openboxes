describe("miscellaneous", function(){

  it("should expire requisitions after 7 days", function(){
    var model = {id:"1234",name:"test"};
    openboxes.saveToLocal("test", model);
    expect(localStorage.openboxesManager).not.toBeNull();
    var d = new Date();
    var manager = JSON.parse(localStorage.openboxesManager);
    d.setDate(d.getDate()-10);
    manager["test"] = d;
    localStorage.openboxesManager = JSON.stringify(manager);
    openboxes.expireFromLocal();
    var retrievedModel = openboxes.getFromLocal("test");
    expect(retrievedModel).toBeNull();
  });

  it("should save and get from local storage", function(){
    var model = {id:"1234",name:"test"};
    openboxes.saveToLocal("test", model);
    var retrievedModel = openboxes.getFromLocal("test");
    expect(localStorage.openboxesManager).not.toBeNull();
    expect(localStorage.openboxesManager["test"]).not.toBeNull();
    console.log(localStorage.openboxesManager["test"]);
    expect(retrievedModel.id).toEqual(model.id);
    expect(retrievedModel.name).toEqual(model.name);
  });

  it("should delete from local", function() {
    var model = {id:"1234",name:"test"}
    openboxes.saveToLocal("test", model);
    openboxes.deleteFromLocal("test");
    var retrievedModel = openboxes.getFromLocal("test");
    expect(retrievedModel).toBeNull();
  });

  it("should save requisition to local", function(){
    var model = new openboxes.requisition.Requisition({id:"1234",name:"test"});
    var key = openboxes.requisition.saveRequisitionToLocal(model);
    expect(key).toEqual("openboxesRequisition1234");
    var retrievedModel = openboxes.getFromLocal(key);
    expect(retrievedModel.name).toEqual(model.name());
    expect(openboxes.requisition.getRequisitionFromLocal(model.id()).id).toEqual(model.id());
  });

  it("should delete requisition from local", function(){
    var model = new openboxes.requisition.Requisition({id:"1234",name:"test"});
    var key = openboxes.requisition.saveRequisitionToLocal(model);
    expect(key).toEqual("openboxesRequisition1234");
    openboxes.requisition.deleteRequisitionFromLocal(model.id());
    var retrievedModel = openboxes.getFromLocal(key);
    expect(retrievedModel).toBeNull();
  });

  it("should not save requistion to local if it has no Id", function(){
    var model =  new openboxes.requisition.Requisition({name:"test"});
    var key = openboxes.requisition.saveRequisitionToLocal(model);
    expect(key).toBeNull();
   });

  it("should save picklist to local with requisition Id", function(){
    var model = new openboxes.requisition.Picklist({id:"1234", requisitionId:"5678"});
    var key = openboxes.requisition.savePicklistToLocal(model);
    expect(key).toEqual("openboxesPicklist5678");
    var retrievedModel = openboxes.getFromLocal(key);
    expect(retrievedModel.requisitionId).toEqual(model.requisitionId());
    expect(openboxes.requisition.getPicklistFromLocal(model.requisitionId()).id).toEqual(model.id());
  });

  it("should not save picklist to local if it has no requisition Id", function(){
    var model =  new openboxes.requisition.Picklist({id:"test"});
    var key = openboxes.requisition.savePicklistToLocal(model);
    expect(key).toBeNull();
   });

  it("should delete requisition from local", function(){
    var model = new openboxes.requisition.Picklist({id:"1234",requisitionId:"5678"});
    var key = openboxes.requisition.savePicklistToLocal(model);
    expect(key).toEqual("openboxesPicklist5678");
    openboxes.requisition.deletePicklistFromLocal(model.requisitionId());
    var retrievedModel = openboxes.getFromLocal(key);
    expect(retrievedModel).toBeNull();
  });
});
