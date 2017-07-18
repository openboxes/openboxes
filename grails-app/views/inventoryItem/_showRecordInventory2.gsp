<div id="inventoryForm">
    <g:form action="saveRecordInventory2" autocomplete="off">

        <g:hiddenField name="productInstance.id" value="${commandInstance.product?.id}"/>
        <g:hiddenField name="inventoryInstance.id" value="${commandInstance?.inventory?.id}"/>

		<div id="debug"></div>
        <div class="form-content box">

            <div class="middle left" style="padding: 10px 0 10px 0;">
                <label><warehouse:message code="inventory.inventoryDate.label"/></label>
                <g:jqueryDatePicker
                        id="transactionDate"
                        name="transactionDate"
                        value="${commandInstance?.transactionDate}"
                        format="MM/dd/yyyy"
                        showTrigger="false" />


                <button class="addAnother" data-bind="click: addItem">
                    <img src="${createLinkTo(dir:'images/icons/silk', file:'add.png') }"/>
                    <warehouse:message code="inventory.addInventoryItem.label"/>
                </button>
            </div>
            <table id="inventoryItemsTable">
                <thead>
                <tr>
                    <th><warehouse:message code="default.lotSerialNo.label"/></th>
                    <th><warehouse:message code="default.expires.label"/></th>
                    <th class="left"><warehouse:message code="inventory.oldQty.label"/></th>
                    <th class="left"><warehouse:message code="inventory.newQty.label"/></th>
                    <th class="left"><warehouse:message code="default.actions.label"/></th>
                </tr>
                </thead>
				<tbody data-bind="foreach: inventoryItems">                	
					<tr>
						<td>
							<input type="text" data-bind="value: lotNumber"/>
						</td>
						<td>
							<input data-bind="value: expirationDate" type="hidden"/>
				            <input type="text" class="required ui_datepicker" max-date="${new Date()}"
				              data-bind="date_picker:{}"/>
						</td>
						<td>
							<span data-bind="text: previousQuantity"></span>
						</td>
						<td>
							<input type="text" data-bind="value: currentQuantity"/>
						</td>
						<td>
							<button data-bind="click: $root.removeItem, enable: id() == undefined">
								<img src="${createLinkTo(dir:'images/icons/silk', file:'cross.png') }"/>
                    			<warehouse:message code="default.button.delete.label"/></button>
						</td>
					</tr>

                </tbody>
            </table>


        </div>
        <div class="center buttons">
            <button name="save" type="submit" class="positive" id="saveInventoryItem" data-bind="click: save">
                <img src="${createLinkTo(dir:'images/icons/silk', file:'accept.png') }"/>&nbsp;
                <warehouse:message code="default.button.save.label"/>&nbsp;
            </button>
            &nbsp;
            <g:link controller="inventoryItem" action="showStockCard"
                    params="['product.id':commandInstance.product?.id]" class="negative"><warehouse:message code="default.button.cancel.label"/></g:link>

        </div>

    </g:form>
</div>

<script>
    $(function(){
        var data = ${product}||{};
        var viewModel = new openboxes.inventory.RecordInventoryViewModel(data.product, data.inventoryItems);
        ko.applyBindings(viewModel);
    });
</script>
