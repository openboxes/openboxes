<%@ page
	import="grails.converters.JSON; org.pih.warehouse.core.RoleType"%>
<%@ page import="org.pih.warehouse.requisition.RequisitionType"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta name="layout" content="custom" />
<g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
<title><warehouse:message code="${requisition?.id ? 'default.edit.label' : 'default.create.label'}" args="[entityName]" /></title>
<script src="${createLinkTo(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript"></script>
<script src="${createLinkTo(dir:'js/', file:'knockout_binding.js')}" type="text/javascript"></script>
<script src="${createLinkTo(dir:'js/', file:'requisition.js')}" type="text/javascript"></script>
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
	<g:if test="${flash.invalidToken}">
		Don't click the button twice!
	</g:if>

	<g:render template="summary" model="[requisition:requisition]"/>
	
	
	<div class="yui-ga">
		<div class="yui-u first">

			<g:form name="requisitionForm" method="post" action="save" useToken="true">
				<g:if test="${requisition?.id }">
					<div class="box">
						<a class="toggle" href="javascript:void(0);">
							<img src="${createLinkTo(dir: 'images/icons/silk', file: 'section_collapsed.png')}" style="vertical-align: bottom;"/>
						</a>
						<h3 style="display: inline" class="toggle">${requisition?.requestNumber } ${requisition?.name }</h3>				
						<g:if test="${requisition?.id }">
							<g:if test="${!params.editHeader }">
								<g:link controller="requisition" action="editHeader" id="${requisition?.id }">
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'pencil.png')}" style="vertical-align: bottom;"/>
								</g:link>
							</g:if>
							<g:else>
								<g:link controller="requisition" action="edit" id="${requisition?.id }">
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'cross.png')}" style="vertical-align: bottom;"/>
								</g:link>									
							</g:else>
						</g:if>
					</div>
					
				</g:if>
				<div id="requisition-header-details" class="dialog ui-validation expandable box" style="${(!requisition?.id||params.editHeader)?'':'display: none;'}">
					<%-- 
					<div id="requisition-header">
						<div class="title" data-bind="html: requisition.name"></div>
						<div class="time-stamp fade"
							data-bind="html:requisition.lastUpdated"></div>
						<div class="status fade" data-bind="html: requisition.status"></div>
					</div>
					--%>
					asfasas
					
					<div class="yui-g">
						<div class="yui-u first">
						
							<table id="requisition-header-details-table" class="header-summary-table">
								
								<tbody>							
									<tr class="prop">
										<td class="name">
											<label for="origin.id">
												<warehouse:message code="requisition.origin.label" />
											</label>
										</td>
										<td class="value ${hasErrors(bean: requisition, field: 'origin', 'errors')}">
											<g:if test="${params?.editHeader || !requisition?.id }">
												<g:select name="origin.id"
													from="${locations}" id="depot"
													data-bind="value: requisition.originId" 
													optionKey="id" optionValue="name" class='required' value=""
													noSelection="['null':'']" />
											</g:if>	
											<g:else>
												${requisition?.origin?.name }
											</g:else>
										</td>
									</tr>
									<tr class="prop">
										<td class="name"><label><warehouse:message
													code="requisition.requestedBy.label" /></label></td>
										<td class="value">
											<g:if test="${params?.editHeader || !requisition?.id }">
												<input name="requestedById"
													data-bind="value: requisition.requestedById" type="hidden" /> <input
													id="requestedBy" name="requestedBy"
													class="autocomplete required text" size="60"
													placeholder="${warehouse.message(code:'requisition.requestedBy.label')}"
													data-bind="autocompleteWithId: {source: '${request.contextPath }/json/searchPersonByName'}, value: requisition.requestedByName" />
											</g:if>
											<g:else>
												${requisition?.requestedBy?.name }
											</g:else>
										</td>
									</tr>
									<tr class="prop">
										<td class="name"><label><warehouse:message
													code="requisition.dateRequested.label" /></label></td>
										<td class="value">
											<g:if test="${params?.editHeader || !requisition?.id }">
												<input
													data-bind="value: requisition.dateRequested" type="hidden" /> <input
													type="text" class="required ui_datepicker text"
													max-date="${new Date()+30}" id="dateRequested"
													data-bind="date_picker:{}" />
											</g:if>
											<g:else>
												${requisition?.dateRequested }
											</g:else>
										</td>
									</tr>
																
									
								</tbody>
							</table>
												
						
						</div>
					
						<div class="yui-u">
						
						
							<table class="">
								<tbody>
									<tr class="prop">
										<td class="name">
											<label for="destination.id"> 
												<warehouse:message code="requisition.destination.label" />
											</label>
										</td>
										<td class="value">	
											${session?.warehouse?.name }
										</td>
									</tr>
									<tr class="prop">
										<td class="name">
											<label><warehouse:message
													code="requisition.processedBy.label" /></label>
										</td>
										<td class="value">
											${requisition?.createdBy?.name?:session?.user?.name }
										</td>
										
										
									</tr>
									<tr class="prop">
										<td class="name"><label><warehouse:message code="requisition.requestedDeliveryDate.label" /></label></td>
										<td class="value">
											<g:if test="${params?.editHeader || !requisition?.id }">
												<input
													data-bind="value: requisition.requestedDeliveryDate" type="hidden" />
													<input class="required ui_datepicker text"
													min-date="${new Date()}" type="text"
													id="requestedDeliveryDate" data-bind="date_picker:{}" />
											</g:if>
											<g:else>
												${requisition?.requestedDeliveryDate }
											</g:else>
										</td>
									</tr>		
									<%--
									<tr class="prop">							
										<td class="name">
											<label for="description"> 
												<warehouse:message code="default.description.label" />
											</label>
										</td>
									
										<td class="value">	
										
											<g:if test="${params?.editHeader || !requisition?.id }">
												<g:textArea name="description" cols="80" rows="5"
													placeholder="${warehouse.message(code:'requisition.description.label')}"
													data-bind="value: requisition.description" class="text">${requisition.description }</g:textArea>
											</g:if>
											<g:else>
												${requisition?.description?:warehouse.message(code:'default.none.label') }
												
											</g:else>
										</td>
									</tr>	
									 --%>
															
								</tbody>
							</table>							
						</div>
					</div>
					
									
					
				</div>
				<g:if test="${requisition?.id && !params.editHeader}">
					<div>
						<table id="requisition-items" class="ui-validation-items requisition"
							data-bind="visible: requisition.requisitionItems().length">
							<thead>
								<tr class="prop odd">
									<th class="list-header">
										${warehouse.message(code: 'requisitionItems.label')}
									</th>
									<th class="list-header center">
										${warehouse.message(code: 'requisitionItem.quantity.label')}
									</th>
									<th class="list-header center">
										${warehouse.message(code: 'product.uom.label', default: 'UOM')}
									</th>
									<th class="center">
										${warehouse.message(code: 'requisitionItem.delete.label')}
									</th>
								</tr>
							<thead>
							<tbody data-bind="foreach: requisition.requisitionItems">
								<tr class="requisitionItemsRow">
									<td class="list-header"><input type="hidden"
										data-bind="value: productId, uniqueName: true" /> <input
										type="text" name="product"
										placeholder="${warehouse.message(code:'requisition.addItem.label')}"
										class="required autocomplete text search-product"
										data-bind="search_product: {source: '${request.contextPath }/json/searchProduct', id:'searchProduct'+$index()}, value: productName"
										size="50" />
									</td>
									<td class="list-header center">
										<input name="quantity" type="text"
											class="required number quantity text center" size="10" data-bind="value: quantity" />
									</td>
									<td class="list-header center middle">
										<div class="unitOfMeasure" data-bind="text: unitOfMeasure"></div>
									</td>
									<td class="list-header"><input type="text"
										data-bind="value: recipient, uniqueName: true" /></td>
									<td class="center middle">
										<a href='#' class="button"
											data-bind='click: $root.requisition.removeItem' tabindex="-1"> 
												${warehouse.message(code:'default.button.delete.label')}
										</a>
									</td>
									
								</tr>
							</tbody>
						</table>
					
					</div>
				</g:if>
				
					
				<div class="buttons">
				
					<g:if test="${requisition?.id && params.editHeader}">
						<button class="button" name="saveAndContinue">${warehouse.message(code:'default.button.saveAndContinue.label', default: 'Save & Continue') }</button>									
						
						&nbsp;
						<g:link controller="requisition" action="edit" id="${requisition?.id }">
							<warehouse:message code="default.button.cancel.label"/>
						</g:link>
						
					</g:if>				
					<g:else>
						<div class="left">
							<g:if test="${requisition?.id && !params.editHeader}">
								<button class="button"
									id="addRequisitionItemRow" name="addRequisitionItemRow" 
									data-bind='click: requisition.addItem'>
										${warehouse.message(code:'requisition.addNewItem.label')}</button>
							</g:if>
						</div>
					
						<div class="right">
							<g:link controller="requisition" action="${requisition?.id ? 'show': 'list'}" id="${requisition?.id }" class="button">
								<warehouse:message code="default.button.back.label"/>	
							</g:link>
							<input type="hidden" data-bind="value: requisition.id" />
							<button id="save-requisition" class="button">
								${warehouse.message(code: 'default.button.next.label')}</button>
						</div>
					</g:else>
				</div>					
			</g:form>
		</div>
	</div>
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
            //if(requestedBy != "") {
            //    description += " - " + requestedBy;
            //}
            description += " - " + dateRequested;
            viewModel.requisition.name(description);
        };


        // Update the description, when changing any value that is a component of the description
        $(".value").change(updateDescription);

        // Set the interval to save locally every 3 seconds
        setInterval(function () {
            openboxes.requisition.saveRequisitionToLocal(viewModel.requisition);
        }, 3000);

        $("#cancelRequisition").click(function() {
            if(confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}')) {
                openboxes.requisition.deleteRequisitionFromLocal(requisitionFromServer.id);
                return true;
            }
        });

        $("input.quantity").keyup(function(){
           this.value=this.value.replace(/[^\d]/,'');      
           $(this).trigger("change");//Safari and IE do not fire change event for us!
        });

  		$("#addRequisitionItemRow").click(function(event){
  			$('#requisition-items tbody tr:last').find('.search-product').focus();
  	  	}); 

		$(".toggle").click(function() {  
			// hides children divs if shown, shows if hidden 
			$("#requisition-header-details").toggle();
		}); 
  		
    });
</script>
</body>
</html>
