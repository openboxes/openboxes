
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'shipment.label', default: 'Shipment')}" />
	<title><warehouse:message code="shipping.receiveShipment.label"/></title>
    <style>
        .border-top { border-top: 3px lightgrey solid; }
    </style>
</head>

<body>
	<div class="body">
	

        <g:render template="summary" />

        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>

        <g:hasErrors bean="${receiptInstance}">
            <div class="errors">
                <g:renderErrors bean="${receiptInstance}" as="list" />
            </div>
        </g:hasErrors>
        <g:hasErrors bean="${shipmentInstance}">
            <div class="errors">
                <g:renderErrors bean="${shipmentInstance}" as="list" />
            </div>
        </g:hasErrors>

        <div class="buttons">
            <div class="button-container" style="text-align: left">
                <g:render template="../shipment/actions" model="[shipmentInstance:shipmentInstance]" />
                <g:render template="buttons"/>

                <g:link controller="shipment" action="deleteReceipt" id="${receiptInstance?.id}" class="button">
                    <img src="${createLinkTo(dir: 'images/icons/silk', file: 'reload.png')}"/>
                    &nbsp;<g:message code="default.button.startOver.label" />
                </g:link>
            </div>
        </div>

		<div class="dialog">
			
			<g:form action="receiveShipment" method="POST">
				<g:hiddenField name="id" value="${shipmentInstance?.id}" />

                <div class="right clearfix">
                    <div class="tag tag-warning">
                        ${g.message(code: 'receipt.saveAndContinue.message')}
                    </div>
                </div>

                <div class="box">
                    <h2>
                        <img src="${createLinkTo(dir:'images/icons',file:'handtruck.png')}"/>
                        <warehouse:message code="receiving.label"/>
                    </h2>

                    <table>
                        <tbody>

                        <tr class="prop">
                            <td class="name middle">
                                <label><warehouse:message code="shipping.shippedOn.label" /></label>
                            </td>
                            <td class="value">
                                <div title="${g.prettyDateFormat(date: shipmentInstance?.actualShippingDate)}">
                                    <g:formatDate date="${shipmentInstance?.actualShippingDate }" format="d MMMMM yyyy hh:mm a z"/>
                                </div>
                            </td>
                        </tr>

                        <tr class="prop">
                            <td valign="middle" class="name middle">
                                <label><warehouse:message code="shipping.deliveredOn.label"/></label>
                            </td>
                            <td valign="top"
                                class="value ${hasErrors(bean: receiptInstance, field: 'actualDeliveryDate', 'errors')}"
                                nowrap="nowrap">
                                <g:datePicker name="actualDeliveryDate" value="${receiptInstance?.actualDeliveryDate}" precision="minute" noSelection="['':'']"/>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name middle">
                                <label><warehouse:message code="shipping.recipient.label" /></label>
                            </td>
                            <td class="value">
                                <g:autoSuggest id="recipient" name="recipient" jsonUrl="${request.contextPath }/json/findPersonByName"
                                               width="300"
                                               styleClass="text"
                                               valueId="${receiptInstance?.recipient?.id}"
                                               valueName="${receiptInstance?.recipient?.name}"/>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>

                <div class="box ${hasErrors(bean: receiptInstance, field: 'receiptItem', 'errors')}">
                    <h2>
                        <warehouse:message code="shipping.itemsToReceive.label" default="Items to be received"/>
                    </h2>
                    <g:if test="${shipmentInstance?.destination.isWarehouse()}">
                        <g:if test="${!receiptInstance.receiptItems}">
                            <div class="center empty">
                                <warehouse:message code="shipping.noItemsToReceive.label" />
                            </div>
                        </g:if>
                        <g:else>

                            <table>
                                <thead>
                                    <tr>
                                        <th>${g.message(code:'packingUnit.label', default: 'Packing unit')}</th>
                                        <th class="center"><g:message code="product.productCode.label" /></th>
                                        <th class="left"><g:message code="product.label" /></th>
                                        <th class="center"><g:message code="default.lotSerialNo.label" /></th>
                                        <th class="center"><g:message code="inventoryItem.expirationDate.label" /></th>
                                        <th class="left"><g:message code="shipping.shipped.label" /></th>
                                        <th class="left"><g:message code="shipping.received.label" /></th>
                                        <th class="left"><g:message code="default.quantity.label" /></th>
                                        <th class="left" colspan="3"><g:message code="location.binLocation.label" /></th>
                                        <th class="left"><g:message code="default.comment.label" /></th>
                                        <th ><g:message code="default.actions.label" /></th>
                                    </tr>
                                </thead>
                                <tbody>

                                    <g:set var="lastContainer"/>

                                    <g:set var="i" value="${0}"/>
                                    <g:each var="shipmentItem" in="${shipmentInstance?.shipmentItems.sort() }" status="outerStatus">
                                        <g:each var="receiptItem" in="${shipmentItem?.receiptItems}" status="innerStatus">
                                            <g:set var="shipmentItem" value="${receiptItem?.shipmentItem }"/>
                                            <g:set var="inventoryItem" value="${receiptItem?.inventoryItem }"/>
                                            <g:set var="isNewContainer" value="${lastContainer != receiptItem?.shipmentItem?.container }"/>
                                            <g:set var="hasErrors" value="${receiptItem?.shipmentItem.quantity != receiptItem?.shipmentItem.quantityReceived() && innerStatus==0}"/>
                                            <tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'} ${innerStatus==0?'border-top':'' }">
                                                <td style="text-align: left;" class="middle">
                                                    <g:if test="${isNewContainer}">
                                                        <g:render template="container" model="[container:shipmentItem?.container,showDetails:false]"/>
                                                    </g:if>
                                                </td>
                                                <td class="center middle">
                                                    ${receiptItem.product.productCode}
                                                </td>
                                                <td class="left middle">
                                                    <g:hiddenField name="receiptItems[${i}].shipmentItem.id" value="${receiptItem?.shipmentItem?.id}"/>
                                                    <g:hiddenField name="receiptItems[${i}].inventoryItem.id" value="${receiptItem?.inventoryItem?.id}"/>
                                                    <g:hiddenField name="receiptItems[${i}].product.id" value="${receiptItem?.product?.id}"/>
                                                    <g:link controller="inventoryItem" action="showStockCard" id="${receiptItem?.product?.id}">
                                                        <format:product product="${receiptItem?.product}"/>
                                                    </g:link>
                                                </td>
                                                <td class="center middle">
                                                    <g:hiddenField name="receiptItems[${i}].lotNumber" value="${receiptItem?.lotNumber}"/>
                                                    ${receiptItem?.lotNumber}
                                                </td>
                                                <td class="center middle">
                                                    <format:expirationDate obj="${inventoryItem?.expirationDate }"/>
                                                </td>
                                                <td class="middle center">
                                                    <g:hiddenField name="receiptItems[${i}].quantityShipped" value="${receiptItem?.quantityShipped}"/>
                                                    <g:if test="${innerStatus == 0}">
                                                        ${receiptItem?.shipmentItem?.quantity}
                                                    </g:if>
                                                </td>
                                                <td class="middle center">
                                                    <g:if test="${innerStatus == 0}">
                                                        <div class="${hasErrors?'errors':''}">${receiptItem?.shipmentItem.quantityReceived()}</div>
                                                    </g:if>
                                                </td>
                                                <td class="middle ${hasErrors?'errors':''}">
                                                    <input class="text" name="receiptItems[${i}].quantityReceived" value="${receiptItem?.quantityReceived}" type="number"
                                                           min="0" />
                                                </td>
                                                <td class="middle left">
                                                    <g:selectBinLocation id="receiptItems-${i}-binLocation" name="receiptItems[${i}].binLocation.id"
                                                                         class="chzn-select-deselect binLocation" noSelection="['null':'']" value="${receiptItem?.binLocation?.id}"/>
                                                </td>
                                                <td class="middle center">
                                                    <a href="javascript:void(-1)" data-id="${receiptItem?.id}" data-index="${i}" class="btnCopyPutaway" title="${warehouse.message(code:'receipt.copyBinLocation.message')}">
                                                        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'page_copy.png')}" class="middle"/>
                                                    </a>
                                                </td>
                                                <td class="middle center">
                                                    <a href="javascript:void(-1)" data-id="${receiptItem?.id}" class="btnShowPutaways" title="${warehouse.message(code:'default.button.zoom.label')}">
                                                        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'zoom.png')}" class="middle"/>
                                                    </a>
                                                </td>

                                                <td class="middle ${hasErrors?'errors':''}">
                                                    <g:textField class="text" name="receiptItems[${i}].comment" value="${receiptItem?.comment}" style="width:100%"/>
                                                </td>
                                                <td>
                                                    <div class="button-group">
                                                        <g:if test="${innerStatus==0}">
                                                            <g:link controller="shipment" action="splitReceiptItem" id="${receiptItem?.id}" class="button">
                                                                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'arrow_switch_bluegreen.png')}"/>
                                                                <g:message code="default.button.split.label"/>
                                                            </g:link>
                                                        </g:if>
                                                        <g:else>
                                                            <g:link controller="shipment" action="deleteReceiptItem" id="${receiptItem?.id}" class="button">
                                                                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'delete.png')}"/>
                                                                <g:message code="default.button.delete.label"/>
                                                            </g:link>
                                                        </g:else>
                                                    </div>
                                                </td>
                                            </tr>
                                            <g:set var="lastContainer" value="${receiptItem?.shipmentItem?.container }"/>
                                            <g:set var="i" value="${i+1}"/>
                                        </g:each>
                                    </g:each>
                                </tbody>
                            </table>
                        </g:else>
                    </g:if>
                </div>
                <div class="buttons center">
                    <div class="button-container">
                        <button type="submit" name="saveButton" value="saveAndContinue" class="button">
                            <img src="${createLinkTo(dir: 'images/icons/silk', file: 'disk.png')}"/>
                            &nbsp;<g:message code="default.button.save.label" />
                        </button>
                        <button type="submit" name="saveButton" value="saveAndExit" class="button">
                            <img src="${createLinkTo(dir: 'images/icons/silk', file: 'application_go.png')}"/>
                            &nbsp;<g:message code="default.button.saveAndExit.label" />
                        </button>
                        <button type="submit" name="saveButton" value="receiveShipment" class="button">
                            <img src="${createLinkTo(dir: 'images/icons', file: 'handtruck.png')}"/>
                            &nbsp;<g:message code="shipping.receiveShipment.label" />
                        </button>
                    </div>
                </div>
            </g:form>
        </div>
    </div>

    <div id="dlgShowPutaways" title="${g.message(code: 'default.show.label', args: [g.message(code:'location.binLocation.label')])}">
        <!-- Contents loaded dynamically -->
    </div>

	<script type="text/javascript">
        $(document).ready(function() {
            $(".tabs").tabs({cookie: {}});

            // Copy putaway location for every row
            $(".btnCopyPutaway").click(function(event){
                let index = $(this).data("index");
                let binLocation = $("#receiptItems-" + index + "-binLocation");
                let binLocations = $(".binLocation");
                $.each(binLocations, function(index, element) {
                   $(element).val(binLocation.val());
                   $(element).trigger("chosen:updated");
                });
            });

            // Show Contents dialog
            $("#dlgShowPutaways").dialog({ autoOpen: false, modal: true, width: 500 });
            $(".btnShowPutaways").click(function(event) {
                var id = $(this).data("id");
                var url = "${request.contextPath}/shipment/showPutawayLocations/" + id;
                $("#dlgShowPutaways").load(url).dialog('open');
                event.preventDefault();
            });
		});
	</script>
</body>
</html>
