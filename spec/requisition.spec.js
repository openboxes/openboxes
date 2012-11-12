var ajaxOptions;
jQuery.ajax = function(options){
    ajaxOptions = options;
  }

describe('requisition view model', function(){
  it('should save data to server', function(){
    var id = "1234";
    var originId = "2";
    var dateRequested = new Date();
    var requestedDeliveryDate = new Date();
    var requestedById = "23";
    var recipientProgram = "test";
    var requisition = new warehouse.Requisition(id,originId,dateRequested, requestedDeliveryDate, requestedById, recipientProgram);
    var viewModel = new warehouse.ViewModel(requisition);
    var formElement ={
      action:"testAction"
    }
    viewModel.save(formElement);
    
    expect(ajaxOptions.data.id).toEqual(id);
    expect(ajaxOptions.data['requestedBy.id']).toEqual(requestedById);
    expect(ajaxOptions.data['origin.id']).toEqual(originId);
    expect(ajaxOptions.data.recipientProgram).toEqual(recipientProgram);
    expect(ajaxOptions.data.dateRequested).toEqual(dateRequested);
    expect(ajaxOptions.data.requestedDeliveryDate).toEqual(requestedDeliveryDate);
    
     
  });
});
