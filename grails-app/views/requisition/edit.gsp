<%@ page
	import="grails.converters.JSON; org.pih.warehouse.core.RoleType"%>
<%@ page import="org.pih.warehouse.requisition.RequisitionType"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta name="layout" content="custom" />
<g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
<title><warehouse:message code="${requisition?.id ? 'default.edit.label' : 'default.create.label'}" args="[entityName]" /></title>
<!--
	<script src="${createLinkTo(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript"></script>
	<script src="${createLinkTo(dir:'js/', file:'knockout_binding.js')}" type="text/javascript"></script>
-->

</head>
<body>

	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<g:hasErrors bean="${requisition}">
		<div class="errors">
			<g:renderErrors bean="${requisition}" as="list" />
		</div>
	</g:hasErrors>
	<g:render template="summary" model="[requisition:requisition]"/>
	<div class="yui-gf">
		<div class="yui-u first">
            <g:render template="header" model="[requisition:requisition]"/>
        </div>
        <div class="yui-u">

			<g:form name="requisitionForm" method="post" action="saveRequisitionItems">
                <g:if test="${requisition?.id}">

                    <div class="box dialog">
                        <h2>
                            <warehouse:message code="requisition.addRequisitionItems.label" default="Add requisition items"/>
                        </h2>

                        <table id="requisition-items" class="ui-validation-items zebra "
                            data-bind="visible: requisition.requisitionItems().length">
                            <thead>

                                <tr class="prop">
                                    <th class="list-header">
                                        ${warehouse.message(code: 'requisitionItems.label')}
                                    </th>
                                    <th class="list-header center">
                                        ${warehouse.message(code: 'requisitionItem.quantity.label')}
                                    </th>
                                <%--
                                <th class="list-header center">
                                    ${warehouse.message(code: 'requisitionItem.totalQuantity.label', default:'Total quantity')}
                                </th>
                                --%>
                                    <th class="list-header middle">
                                        ${warehouse.message(code: 'requisitionItem.status.label')}
                                    </th>
                                    <th class="center">
                                        ${warehouse.message(code: 'requisitionItem.delete.label')}
                                    </th>
                                </tr>
                            <thead>
                            <tbody data-bind="foreach: requisition.requisitionItems">
                                <tr class="requisitionItemsRow prop">
                                    <td class="middle list-header">
                                        <input type="hidden" data-bind="value: productPackageId, uniqueName: true" class="productPackageId"/>
                                        <input type="hidden" data-bind="value: productId, uniqueName: true" class="productId" />
                                        <input type="text" name="product" size="80"
                                               placeholder="${warehouse.message(code:'requisition.addItem.label')}"
                                               class="required autocomplete text search-product"
                                               data-bind="search_product: {source: '${request.contextPath }/json/searchProduct', id:'searchProduct'+$index()}, value: productName" />
                                    </td>
                                    <td class="middle list-header center">
                                        <input name="quantity" type="text"
                                            class="required number quantity text center" size="10" data-bind="value: quantity" />
                                    </td>
                                    <td class="middle list-header">
                                        <div data-bind="text: status"></div>
                                    </td>
                                    <td class="center middle">
                                        <a href='#' class="button"
                                            data-bind='click: $root.requisition.removeItem' tabindex="-1">
                                                ${warehouse.message(code:'default.button.delete.label')}
                                        </a>
                                    </td>

                                </tr>
                            </tbody>
                            <tfoot>
                                <tr>
                                    <td colspan="5">
                                        <div class="right">
                                            <g:if test="${requisition?.id}">
                                                <button class="button"
                                                        id="addRequisitionItemRow" name="addRequisitionItemRow"
                                                        data-bind='click: requisition.addItem'>
                                                    ${warehouse.message(code:'requisition.addNewItem.label')}</button>

                                            </g:if>
                                        </div>
									</td>
								</tr>
								<tr>
									<td colspan="5">
										<div class="buttons center">
											<input type="hidden" data-bind="value: requisition.id" />

											<g:link controller="requisition" action="${requisition?.id ? 'show': 'list'}" id="${requisition?.id }" class="button icon arrowleft">
												<warehouse:message code="default.button.back.label"/>
											</g:link>
											<button id="save-requisition" class="button icon arrowright">
												${warehouse.message(code: 'default.button.next.label')}</button>
										</div>

                                    </td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </g:if>
			</g:form>
		</div>
	</div>
	<script src="${createLinkTo(dir:'js/', file:'requisition.js')}" type="text/javascript"></script>
	<script type="text/javascript">
	$(function () {

		// Hack to make the requisition type in the name more pretty (need to internationalize this)
		var requisitionTypes = new Object();
		requisitionTypes['ADHOC'] = 'Adhoc';
		requisitionTypes['STOCK'] = 'Stock';
		requisitionTypes['NON_STOCK'] = 'Non Stock';

		var requisitionFromServer = ${requisition.toJson() as JSON};
		var requisitionFromLocal = openboxes.requisition.getRequisitionFromLocal(requisitionFromServer.id);
		var requisitionData = openboxes.requisition.Requisition.getNewer(requisitionFromServer, requisitionFromLocal);
		var viewModel = new openboxes.requisition.EditRequisitionViewModel(requisitionData);
		var requisitionId = viewModel.requisition.id();
		viewModel.savedCallback = function(){
			if(!requisitionId) {
				window.location = "${request.contextPath}/requisition/edit/" + viewModel.requisition.id();
			} else {
				window.location = "${request.contextPath}/requisition/review/" + viewModel.requisition.id();
			}
		};
		ko.applyBindings(viewModel);


		$("#requisitionForm").validate({
			submitHandler: viewModel.save,
			rules:  {
				product: { required: true },
				quantity: { required: true, min: 1 },
				requestedBy: { required: true }
			},
			messages: {
				product: { required: "${warehouse.message(code: 'inventoryItem.productNotSupported.message')}" },
				requestedBy: { required: "${warehouse.message(code: 'person.notFound.message')}" }
			}
		});


		if (!viewModel.requisition.name())
			viewModel.requisition.name("" + requisitionTypes[viewModel.requisition.type()] + " " + "${warehouse.message(code: 'requisition.label')}");

		var updateDescription = function () {
			var type = requisitionTypes[viewModel.requisition.type()] + " ";
			var depot = $("select#depot option:selected").text() || "";
			var program = $("#recipientProgram").val() || "";
			var requestedBy = $("#requestedBy").val() || "";
			var dateRequested = $("#dateRequested").val() || "";
			var description = type + "${warehouse.message(code: 'requisition.label', default: 'Requisition')}";
			if(depot) {
				description += " - " + depot;
			}
			if(program != "") {
				description += " - " + program;
			}
			description += " - " + dateRequested;
			viewModel.requisition.name(description);
		};

		// Update the description, when changing any value that is a component of the description
		$(".value").change(updateDescription);

        // Make sure quantity is a digit
		$("input.quantity").keyup(function(){
		    this.value=this.value.replace(/[^\d]/,'');
		    $(this).trigger("change");//Safari and IE do not fire change event for us!
		});

        // On page load, put focus on product search box
        $('#requisition-items tbody tr:last').find('.search-product').focus();

        // Any time we add a new row, put focus on search box
		$("#addRequisitionItemRow").click(function(event){
			$('#requisition-items tbody tr:last').find('.search-product').focus();
		});



	});
</script>
</body>
</html>
