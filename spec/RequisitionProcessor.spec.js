
describe('Requisition Processor View Model', function(){
    it('has requisitionItems', function() {

        // creating the view model
        var viewModel = ko.mapping.fromJS(mocks.mockRequisitionProcessorServerData, RequisitionProcessor.mapping);

        // testing the view model properties that we create, not the ones that the ko.mapping plugin does

        expect(viewModel.requisitionItems().length).toEqual(3);
        expect(viewModel.requisitionItems()[0].rowIndex()).toEqual(1);
        expect(viewModel.requisitionItems()[0].quantity()).toEqual(mocks.mockRequisitionProcessorServerData.requisitionItems[0].quantity);
        expect(viewModel.requisitionItems()[0].quantityPicked()).toEqual(0);
        expect(viewModel.requisitionItems()[0].quantityRemaining()).toEqual(viewModel.requisitionItems()[0].quantity() - viewModel.requisitionItems()[0].quantityPicked());

    });
});