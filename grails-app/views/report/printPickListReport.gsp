<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="print" />
	    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'print.css')}" type="text/css" media="print, screen, projection" />
        <title><warehouse:message code="report.showShippingReport.label" /></title>    
    </head>
    <body>
        <div id="print-button">
            <button type="button"  onclick="window.print()">
                <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />
                ${warehouse.message(code:"default.print.label")}
            </button>
            <button onclick="window.close()">
                <img src="${resource(dir: 'images/icons/silk', file: 'printer_cancel.png')}" />
                ${warehouse.message(code:"default.button.close.label")}
            </button>
        </div>

		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<g:hasErrors bean="${command}">
			<div class="errors">
				<g:renderErrors bean="${command}" as="list" />
			</div>
		</g:hasErrors>
		<g:if test="${command?.shipment }">
			<div>
				<div class="dialog">
		   			<g:set var="status" value="${0 }"/>
			    	<g:set var="pickListByContainer" value="${command?.checklistReportEntryList?.groupBy { it?.shipmentItem?.container } }"/>
		   			<g:each var="picklistItem" in="${pickListByContainer}">
		   			
		   				<div class="page" style="page-break-after: always;">
                            <table id="picklist-items" class="fs-repeat-header">
                                <tr>
                                    <td colspan="9" class="center" >
                                        <table border="0">
                                            <thead>
                                                <tr>
                                                    <td class="left middle" width="1%">
                                                        <g:displayLogo location="${session?.warehouse?.id}" showLabel="${false}"/>
                                                    </td>
                                                    <td class="left middle">
                                                        <div class="title">
                                                            <warehouse:message code="shipping.pickList.label" default="Pick List"/>
                                                        </div>
                                                        <div class="subtitle">
                                                            ${command?.shipment?.name?.encodeAsHTML() }
                                                        </div>
                                                    </td>
                                                    <td class="right middle" width="1%">
                                                        <g:displayBarcode data="${command?.shipment?.shipmentNumber}" height="50" width="120" showData="${true}"/>
                                                    </td>
                                                </tr>
                                            </thead>
                                        </table>
                                        <div class="center">
                                            <table class="center">
                                                <tr class="prop">
                                                    <td>
                                                        <label><warehouse:message code="shipping.shipmentNumber.label"/></label>

                                                    </td>
                                                    <td>
                                                        ${command?.shipment?.shipmentNumber?.encodeAsHTML() }
                                                    </td>
                                                </tr>
                                                <tr class="prop">
                                                    <td>
                                                        <label><warehouse:message code="shipping.expectedShippingDate.label"/></label>

                                                    </td>
                                                    <td>
                                                        <g:formatDate date="${command?.shipment?.expectedShippingDate }"/>
                                                    </td>
                                                </tr>
                                                <tr class="prop">
                                                    <td>
                                                        <label><warehouse:message code="shipping.expectedDeliveryDate.label"/></label>

                                                    </td>
                                                    <td>
                                                        <g:formatDate date="${command?.shipment?.expectedDeliveryDate }"/>
                                                    </td>
                                                </tr>
                                                <tr class="prop">
                                                    <td>
                                                        <label><warehouse:message code="shipping.origin.label"/></label>

                                                    </td>
                                                    <td>
                                                        ${command?.shipment?.origin?.name?.encodeAsHTML() }
                                                    </td>
                                                </tr>
                                                <tr class="prop">
                                                    <td>
                                                        <label><warehouse:message code="shipping.destination.label"/></label>
                                                    </td>
                                                    <td>
                                                        ${command?.shipment?.destination?.name?.encodeAsHTML() }
                                                    </td>
                                                </tr>
                                            </table>
                                        </div>


                                    </td>
                                </tr>
                            </table>


                            <div class="box">
                                <h2>
                                    <g:render template="/shipment/container" model="[container:packingListEntry?.key]"/>
                                </h2>

                                <table>
                                    <thead>
                                        <tr>
                                            <th class="center bottom">
                                                <warehouse:message code="report.number.label"/>
                                            </th>
                                            <th class="center">
                                                <warehouse:message code="product.productCode.label"/>
                                            </th>
                                            <th class="bottom">
                                                <warehouse:message code="product.description.label"/>
                                            </th>
                                            <th class="center bottom">
                                                <warehouse:message code="inventoryItem.lotNumber.label"/>
                                            </th>
                                            <th class="center bottom">
                                                <warehouse:message code="inventoryItem.expirationDate.label"/>
                                            </th>
                                            <th class="center bottom">
                                                <warehouse:message code="location.binLocations.label"/>
                                            </th>
                                            <th class="center">
                                                <warehouse:message code="default.quantity.label"/>
                                            </th>
                                            <th class="center">
                                                <warehouse:message code="default.uom.label"/>
                                            </th>
                                        </tr>
                                    </thead>

                                    <tbody>
                                        <g:each var="entry" in="${picklistItem?.value }" status="i">
                                            <tr class="prop ${i % 2 ? 'even' : 'odd' }">
                                                <td class="center">
                                                    ${i+1 }
                                                </td>
                                                <td>
                                                    <g:displayBarcode data="${entry?.shipmentItem?.inventoryItem?.product?.productCode?:entry?.shipmentItem?.product?.productCode}"
                                                                       showData="${true}"/>
                                                </td>
                                                <td>
                                                    <format:product product="${entry?.shipmentItem?.inventoryItem?.product?:entry?.shipmentItem?.product}"/>
                                                </td>
                                                <td>
                                                    ${entry?.shipmentItem?.inventoryItem?.lotNumber?:entry?.shipmentItem?.lotNumber  }
                                                </td>
                                                <td>
                                                    <format:expirationDate obj="${entry?.shipmentItem?.inventoryItem?.expirationDate?:entry?.shipmentItem?.expirationDate }"/>
                                                </td>
                                                <td>
                                                    <g:each var="binLocationEntry" in="${binLocations[entry?.shipmentItem?.inventoryItem]}">
                                                        <div>
                                                            <label>${binLocationEntry?.binLocation?.name?:g.message(code:'default.label')}:</label> ${binLocationEntry?.quantity}
                                                        </div>
                                                    </g:each>

                                                </td>
                                                <td class="center">
                                                    ${entry?.shipmentItem?.quantity }
                                                </td>
                                                <td>
                                                    ${entry?.shipmentItem?.inventoryItem?.product?.unitOfMeasure?:entry?.shipmentItem?.product?.unitOfMeasure}
                                                </td>
                                            </tr>
                                        </g:each>
                                    </tbody>
                                </table>
                            </div>

                            <div style="margin: 10px;">
                                <label class="block">
                                    <warehouse:message code="default.comments.label"/>
                                </label>
                                <div style="border: 1px solid lightgrey; width: 100%; height: 200px;">
                                    &nbsp;
                                </div>
                            </div>
                        </div>
					</g:each>
				</div>		
			</div>
		</g:if>
    </body>
</html>