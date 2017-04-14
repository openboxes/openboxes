  
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title><warehouse:message code="shipping.pickShipmentItems.label"/></title>
         <style>
         	.top-border { border-top: 2px solid lightgrey; }
         	.right-border { border-right: 2px solid lightgrey; }
         </style>
    </head>
    <body>
        <div class="body">
            ${flash.message}
        	<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
            <g:if test="${message}">
                <div class="message">${message}</div>
            </g:if>
            <g:hasErrors bean="${command}">
				<div class="errors">
					<g:renderErrors bean="${command}" as="list" />
				</div>
			</g:hasErrors>
			<g:hasErrors bean="${shipmentInstance}">
				<div class="errors">
					<g:renderErrors bean="${shipmentInstance}" as="list" />
				</div>
			</g:hasErrors>



			<g:form action="createShipment" method="post">
				<g:hiddenField name="id" value="${shipmentInstance?.id}"/>
				<g:hiddenField name="shipment.id" value="${shipmentInstance?.id}"/>

                <g:render template="../shipment/summary" />
                <g:render template="flowHeader" model="['currentState':'Picking']"/>

                <div class="dialog box">
                    <h2>
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'basket_put.png')}"/>
                        <warehouse:message code="shipping.pickShipmentItems.label"/>
                    </h2>
                    <table>
                        <thead>

                        <tr>
                            <th class="right-border"><warehouse:message code="container.label"/></th>
                            <th class="middle center"><input type="checkbox" class="checkAll"/></th>
                            <th><warehouse:message code="product.label"/></th>
                            <th><warehouse:message code="location.binLocation.label"/></th>
                            <th><warehouse:message code="inventoryItem.lotNumber.label"/></th>
                            <th><warehouse:message code="inventoryItem.expirationDate.label"/></th>
                            <th colspan="2" class="center"><warehouse:message code="default.quantity.label"/></th>
                            <th></th>
                        </tr>
                        </thead>
                        <g:set var="previousContainer"/>
                        <tbody>
                            <g:each var="shipmentItem" in="${shipmentInstance?.shipmentItems.sort() }" status="status">
                                <g:set var="isSameAsPrevious" value="${shipmentItem?.container == previousContainer}"/>
                                <tr class="${status % 2 ? 'even' : 'odd' } ${!isSameAsPrevious ? 'top-border':'' }">
                                    <td class="right-border">
                                        <g:if test="${!isSameAsPrevious }">
                                            <g:if test="${shipmentItem?.container}">
                                                ${shipmentItem?.container?.name }
                                            </g:if>
                                            <g:else>
                                                <g:message code="default.label"/>
                                            </g:else>
                                        </g:if>
                                    </td>
                                    <td class="middle center">
                                        <g:checkBox class="shipment-item" name="shipmentItem.id" value="${shipmentItem?.id}" checked="${false}"/>
                                    </td>
                                    <td class="middle">
                                        <g:link controller="inventoryItem" action="showStockCard" params="['product.id':shipmentItem?.inventoryItem?.product?.id]">
                                            ${shipmentItem?.inventoryItem?.product?.productCode}
                                            <format:product product="${shipmentItem?.inventoryItem?.product}"/>
                                        </g:link>
                                    </td>
                                    <td class="middle">
                                        ${shipmentItem?.binLocation}
                                    </td>
                                    <td class="middle">
                                        <span class="lotNumber">${shipmentItem?.inventoryItem?.lotNumber }</span>
                                    </td>
                                    <td class="middle">
                                        <g:if test="${shipmentItem?.inventoryItem?.expirationDate}">
                                            <span class="expirationDate">
                                                <g:formatDate date="${shipmentItem?.inventoryItem?.expirationDate }" format="d MMMMM yyyy"/>
                                            </span>
                                        </g:if>
                                        <g:else>
                                            <span class="fade">
                                                ${warehouse.message(code: 'default.never.label')}
                                            </span>
                                        </g:else>
                                    </td>
                                    <td class="middle right">
                                        ${shipmentItem?.quantity }
                                    </td>
                                    <td class="middle left">
                                        ${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }
                                    </td>
                                    <td class="middle">
                                        <a href="javascript:void(-1)" data-id="${shipmentItem?.id}" data-execution="${params.execution}"
                                           class="btnPickItem button"><g:message code="shipping.pickShipmentItem.label"/></a>
                                    </td>

                                </tr>
                                <g:set var="previousContainer" value="${shipmentItem?.container }"/>
                            </g:each>
                        </tbody>
                        <tfoot>

                        <tr>
                            <td>
                            </td>
                            <td colspan="8">
                                <div class="left">
                                    <g:submitButton name="autoPickShipmentItems" value="${g.message(code:'shipping.autoPickItems.label')}" class="button"></g:submitButton>
                                </div>

                            </td>
                        </tr>
                        </tfoot>

                    </table>


                </div>
                <div class="buttons">
                    <button name="_eventId_back" class="button">&lsaquo; <warehouse:message code="default.button.back.label"/></button>
                    <button name="_eventId_next" class="button"><warehouse:message code="default.button.next.label"/> &rsaquo;</button>
                    <button name="_eventId_save" class="button"><warehouse:message code="default.button.saveAndExit.label"/></button>
                    <button name="_eventId_cancel" class="button"><warehouse:message code="default.button.cancel.label"/></button>
                </div>

			</g:form>
		</div>

        <div id="dlgPickItem" title="Pick Item">
        </div>

		<script type="text/javascript">
			$(function() {
                $(":checkbox.checkAll").change(function () {
                    $(":checkbox.shipment-item").prop('checked', $(this).prop("checked"));
                });
                $("#dlgPickItem").dialog({
                    autoOpen: false,
                    modal: true,
                    width: 600
                });

                $(".btnPickItem").click(function(event){
                    var id = $(this).data("id");
                    var executionKey = $(this).data("execution");
                    console.log(executionKey);
                    var url = "${request.contextPath}/shipmentItem/pick/" + id + "?execution=" + executionKey;
                    $("#dlgPickItem").load(url).dialog("open");
                });

                $(".btnCloseDialog").click(function(event){
                    $('#dlgPickItem').dialog("close");
                });


			});
		</script>
		
	</body>
</html>
