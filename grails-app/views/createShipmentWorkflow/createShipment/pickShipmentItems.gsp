  
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

                    <g:if test="${session.warehouse == shipmentInstance?.origin}">

                        <table>
                            <thead>

                            <tr>
                                <th class="right-border"><g:message code="container.label"/></th>
                                <th class="middle center"><input type="checkbox" class="checkAll"/></th>
                                <th><g:message code="product.label"/></th>
                                <th><g:message code="location.binLocation.label"/></th>
                                <th><g:message code="inventoryItem.lotNumber.label"/></th>
                                <th><g:message code="inventoryItem.expirationDate.label"/></th>
                                <th class="center"><g:message code="default.qty.label"/></th>
                                <th class="center"><g:message code="default.uom.label"/></th>
                                <th class="border-right"><g:message code="default.actions.label"/></th>
                                <th class="center" style="padding:0;margin:0">
                                    <g:message code="shipping.availableQuantity.label"/>
                                </th>
                            </tr>
                            </thead>
                            <g:set var="previousContainer"/>
                            <tbody>
                                <g:each var="shipmentItem" in="${shipmentInstance?.shipmentItems.sort() }" status="status">
                                    <g:set var="isSameAsPrevious" value="${shipmentItem?.container == previousContainer}"/>
                                    <tr class="prop ${status % 2 ? 'even' : 'odd' } ${!isSameAsPrevious ? 'top-border':'' }">
                                        <td class="top right-border">
                                            <g:if test="${!isSameAsPrevious }">
                                                <g:if test="${shipmentItem?.container}">
                                                    ${shipmentItem?.container?.name }
                                                </g:if>
                                                <g:else>
                                                    <g:message code="default.label"/>
                                                </g:else>
                                            </g:if>
                                        </td>
                                        <td class="top center">
                                            <g:checkBox class="shipment-item" name="shipmentItem.id" value="${shipmentItem?.id}" checked="${false}"/>
                                        </td>
                                        <td class="top">
                                            <g:link controller="inventoryItem" action="showStockCard" params="['product.id':shipmentItem?.inventoryItem?.product?.id]">
                                                ${shipmentItem?.inventoryItem?.product?.productCode}
                                                <format:product product="${shipmentItem?.inventoryItem?.product}"/>
                                            </g:link>
                                        </td>
                                        <td class="top">
                                            ${shipmentItem?.binLocation}
                                        </td>
                                        <td class="top">
                                            <span class="lotNumber">${shipmentItem?.inventoryItem?.lotNumber }</span>
                                        </td>
                                        <td class="top">
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
                                        <td class="top right">
                                            ${shipmentItem?.quantity }
                                        </td>
                                        <td class="top left ">
                                            ${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }
                                        </td>
                                        <td class="top center border-right">
                                            <a href="javascript:void(-1)" data-id="${shipmentItem?.id}" data-execution="${params.execution}"
                                               class="btnPickItem button"><g:message code="shipping.pickShipmentItem.label"/></a>
                                        </td>
                                        <td class="top border-right" style="padding: 0; margin: 0">


                                            <g:set var="binLocations" value="${quantityMap[shipmentItem?.inventoryItem?.product]}"/>

                                            <g:if test="${!binLocations}">
                                                <div class="empty center">
                                                    <g:message code="inventoryItem.notAvailable.message" args="[shipmentItem?.inventoryItem?.lotNumber, session.warehouse.name]"></g:message>
                                                </div>
                                            </g:if>
                                            <table>
                                                <g:if test="${status==0}">
                                                    <thead>
                                                        <tr>
                                                            <th><g:message code="default.status.label" default="Status"/></th>
                                                            <th><g:message code="default.bin.label" default="Bin"/></th>
                                                            <th><g:message code="default.lot.label" default="Lot"/></th>
                                                            <th><g:message code="default.exp.label" default="Exp"/></th>
                                                            <th><g:message code="default.qty.label" default="Qty"/></th>
                                                            <th><g:message code="default.uom.label" default="UOM"/></th>

                                                        </tr>
                                                    </thead>
                                                </g:if>
                                                <tbody>
                                                <g:each var="entry" in="${binLocations}">
                                                    <g:set var="fade" value="${entry.quantity<shipmentItem?.quantity}"/>
                                                    <g:set var="picked" value="${entry.inventoryItem==shipmentItem?.inventoryItem&&entry.binLocation==shipmentItem?.binLocation}"/>
                                                    <tr class="${fade}">
                                                        <%--
                                                        <td>
                                                            <g:radio name="binLocation.id" value="${entry?.binLocation?.id}"
                                                                     checked="${shipmentItemInstance?.binLocation?.id==entry?.binLocation?.id}"/>
                                                        </td>
                                                        --%>
                                                        <td>
                                                            <g:if test="${picked}">
                                                                <img src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}" title="${g.message(code:'picklist.picked.label')}">
                                                            </g:if>
                                                            <g:else>
                                                            </g:else>

                                                        </td>
                                                        <td class="left" width="20%">
                                                            ${entry?.binLocation?.name?:'Default'}
                                                        </td>
                                                        <td class="left" width="30%">
                                                            <div style="text-overflow: clip ellipsis">

                                                            <g:if test="${entry?.inventoryItem?.lotNumber}">
                                                                ${entry?.inventoryItem?.lotNumber}
                                                            </g:if>
                                                            <g:else>
                                                                <g:message code="default.empty.label"/>
                                                            </g:else>
                                                            </div>
                                                        </td>
                                                        <td class="left" width="20%">
                                                            <g:if test="${entry?.inventoryItem?.expirationDate}">
                                                                <g:formatDate date="${entry?.inventoryItem?.expirationDate}" format="MMM/yyyy"/>
                                                            </g:if>
                                                            <g:else>
                                                                <g:message code="default.never.label"/>
                                                            </g:else>
                                                        </td>
                                                        <td class="left" width="10%">
                                                            ${entry?.quantity}
                                                        </td>
                                                        <td class="left" width="10%">
                                                            ${entry?.product?.unitOfMeasure?:"EA"}
                                                        </td>
                                                        <%--
                                                        <td>
                                                            <g:if test="${entry.quantity>=shipmentItem?.quantity}">
                                                                <img src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}" >
                                                            </g:if>
                                                            <g:else>
                                                                <img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" >
                                                            </g:else>
                                                        </td>
                                                        --%>
                                                    </tr>
                                                </g:each>

                                                </tbody>
                                            </table>

                                        </td>

                                    </tr>
                                    <g:set var="previousContainer" value="${shipmentItem?.container }"/>
                                </g:each>
                            </tbody>
                            <tfoot>

                            <tr>
                                <td>
                                </td>
                                <td colspan="9">
                                    <div class="left">
                                        <g:submitButton name="autoPickShipmentItems" value="${g.message(code:'shipping.autoPickItems.label')}" class="button"></g:submitButton>
                                    </div>

                                </td>
                            </tr>
                            </tfoot>

                        </table>
                    </g:if>
                    <g:else>
                        <div class="empty fade center">
                            <g:message code="shipping.pickShipmentItemsForOutbound.message"/>
                        </div>
                    </g:else>

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
                    width: 800
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
