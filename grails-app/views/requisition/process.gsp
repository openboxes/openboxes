<%@ page import="org.pih.warehouse.requisition.Requisition"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<script
	src="${createLinkTo(dir:'js/knockout/', file:'knockout-2.2.0.js')}"
	type="text/javascript"></script>
<script src="${createLinkTo(dir:'js/', file:'requisition.js')}"
	type="text/javascript"></script>
<g:set var="entityName"
	value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
<title><warehouse:message code="requisition.process.label" /></title>
<content tag="pageTitle">
<warehouse:message code="requisition.process.label" /></content>
</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<div id="picklist" class="left ui-validation">
			<div id="requisition-header">
				<div class="title" id="description"
					data-bind="html: requisition.name"></div>
				<div class="time-stamp fade"
					data-bind="text: requisition.lastUpdated"></div>
				<div class="status fade">
					<span data-bind="text: requisition.status"></span>
				</div>
			</div>

			<g:form name="requisitionForm" method="post" action="save"
				controller="picklist">
				<div class="dialog">
					<ul id="accordion"
						data-bind="foreach: requisition.requisitionItems">						
						<li>
							<div class="accordion-header">
								<div data-bind="text: $index()+1" class="row-index"></div>
								<div data-bind="text: productName" class="product-name"></div>
								<div class="quantity">
									${warehouse.message(code: 'requisitionItem.quantityRequested.label')}:
									<span data-bind="text:quantity"></span>
								</div>
								<div class="quantityPicked">
									${warehouse.message(code: 'requisitionItem.quantityPicked.label')}:
									<span data-bind="text:quantityPicked"></span>
								</div>
								<div class="quantityRemaining">
									${warehouse.message(code: 'requisitionItem.quantityRemaining.label')}:
									<span data-bind="text:quantityRemaining"></span>
								</div>

								<div class="status right">
									<div data-bind="css:status" class="requisition-status right"></div>
									%{--
									<div data-bind="css: { ok: substitutable() }"
										class="substitution right"></div>
									--}%
								</div>
								<div class="clear"></div>
							</div>
							<div class="accordion-content" style="display: none; width: 100%">
								<div class="requisitionItemPicklist">
									<div class="picklist-header">
										<div class="product-name">
											<warehouse:message code="inventoryItem.item.label" />
										</div>
										<div class="lot">
											<warehouse:message code="inventoryItem.lot.label" />
										</div>
										<div class="expiration-date">
											<warehouse:message code="inventoryItem.exp.label" />
										</div>
										<div class="quantity-onhand">
											<warehouse:message code="inventoryItem.onHandQuantity.label" />
										</div>
										<div class="quantity-picked">
											<warehouse:message code="inventoryItem.quantityPicked.label" />
										</div>
										<div class="clear"></div>
									</div>
									<div class="picklist-items ui-validation-items"
										data-bind="foreach: picklistItems()">
										<div data-bind="css: { 'odd': ($index() % 2 == 1) }"
											class="picking-item">
											<div class="product-name picklist-field"
												data-bind="text: $parent.productName"></div>
											<div class="lot picklist-field" data-bind="text: lotNumber"></div>
											<div class="expiration-date picklist-field"
												data-bind="text: expirationDate"></div>
											<div class="quantity-onhand picklist-field"
												data-bind="text: quantityOnHand"></div>
											<div class="quantity-picked">
												<input data-bind="value: quantity" type="text"
													class="number text"></input>
											</div>
											<div class="clear"></div>
										</div>
									</div>
									<div data-bind="if: picklistItems().length == 0">
										<div class="error">
											<warehouse:message code="requisition.picklistItems.message" default="No picklist items available"></warehouse:message>	
										</div>
									</div>
									
								</div>
							</div>
						</li>
					</ul>

				</div>
				<div class="center footer">
					<input type="submit" class="button" id="save-requisition"
						value="${warehouse.message(code: 'default.button.submit.label')}" />
					<g:link action="show" id="${requisitionId}">
						<input type="button" class="button" id="cancelRequisition"
							value="${warehouse.message(code: 'default.button.cancel.label')}" />
					</g:link>
				</div>


			</g:form>
		</div>
		<div class=" right" id="requisition-status-key">
			<fieldset>
				<legend class="left">
					<warehouse:message code="requisitionItem.legend.label" />
				</legend>
				<div class="Complete left requisition-status"></div>
				<div class="left">
					<warehouse:message code="requisitionItem.complete.label" />
				</div>
				<div class="clear"></div>
				<div class="PartiallyComplete left requisition-status"></div>
				<div class="left">
					<warehouse:message code="requisitionItem.partiallycomplete.label" />
				</div>
				<div class="clear"></div>
				<div class="Incomplete left requisition-status"></div>
				<div class="left">
					<warehouse:message code="requisitionItem.incomplete.label" />
				</div>
				<div class="clear"></div>
				<div class="substitution ok left"></div>
				<div class="left">
					<warehouse:message code="requisitionItem.substitutable.label" />
				</div>
				<div class="clear"></div>
			</fieldset>
		</div>
		<div class="clear"></div>
	</div>

	<script type="text/javascript">
	
    $(function(){
    	var viewModel;
        var data = ${data};
        var picklistFromServer = data.picklist;
        var picklistFromLocal = openboxes.requisition.getPicklistFromLocal(data.requisition.id); 
        var newerPicklist = openboxes.requisition.Picklist.getNewer(picklistFromServer, picklistFromLocal);
        viewModel = new openboxes.requisition.ProcessViewModel(data.requisition, newerPicklist, data.productInventoryItemsMap);

        var requisitionId = viewModel.requisition.id();
        viewModel.savedCallback = function(){
            saveToLocal();
            window.location = "${request.contextPath}/requisition/show/" + viewModel.requisition.id();
        };

        ko.applyBindings(viewModel);

        $("#requisitionForm").validate({ submitHandler: viewModel.save });

        $("#accordion").accordion({
          header: ".accordion-header", 
          icons: false, 
          active:false,
          collapsible: true,
          heightStyle: "content"
          });

        setInterval(function () { saveToLocal(); }, 3000);

        $("#cancelRequisition").click(function() {
            if(confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}')) {
                openboxes.requisition.deletePicklistFromLocal(picklistFromServer.id);
                return true;
            }
        });

        $(".quantity-picked input").keyup(function(){
           this.value=this.value.replace(/[^\d]/,'');      
           $(this).trigger("change");//Safari and IE do not fire change event for us!
        });

        function saveToLocal(){
           viewModel.requisition.picklist.updatePickedItems();
           openboxes.requisition.savePicklistToLocal(viewModel.requisition.picklist);
        }

    });
</script>

</body>
</html>
