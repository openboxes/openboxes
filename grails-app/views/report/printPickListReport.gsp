<%@ page defaultCodec="html" %>
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
                ${warehouse.message(code:"default.button.print.label")}
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
                                        <div class="left" style="width:50%">
                                            <table>
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
                                                        <g:formatDate date="${command?.shipment?.expectedShippingDate }" format="MMM dd, yyyy hh:mma z"/>
                                                    </td>
                                                </tr>
                                                <tr class="prop">
                                                    <td>
                                                        <label><warehouse:message code="shipping.expectedDeliveryDate.label"/></label>

                                                    </td>
                                                    <td>
                                                        <g:formatDate date="${command?.shipment?.expectedDeliveryDate }" format="MMM dd, yyyy hh:mma z"/>
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
                                        <div class="right" style="width:50%">
                                            <table>
                                                <tr class="prop">
                                                    <td>
                                                        <label><warehouse:message code="shipping.pickedBy.label" default="Picked by"/></label>
                                                    </td>
                                                    <td>
                                                    </td>
                                                </tr>
                                                <tr class="prop">
                                                    <td>
                                                        <label><warehouse:message code="shipping.verifiedBy.label" default="Verified by"/></label>
                                                    </td>
                                                    <td>
                                                    </td>
                                                </tr>
                                                <tr class="prop">
                                                    <td>
                                                        <label><warehouse:message code="shipping.printedBy.label" default="Printed by"/></label>
                                                    </td>
                                                    <td>
                                                        ${session?.user?.name}
                                                    </td>
                                                </tr>
                                                <tr class="prop">
                                                    <td>
                                                        <label><warehouse:message code="shipping.printedOn.label" default="Printed on"/></label>
                                                    </td>
                                                    <td>
                                                        <g:formatDate date="${new Date()}" format="MMM dd, yyyy hh:mma z"/>
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
                                            <th class="center middle">
                                                <warehouse:message code="report.number.label"/>
                                            </th>
                                            <th class="center">
                                                <warehouse:message code="product.productCode.label"/>
                                            </th>
                                            <th class="middle">
                                                <warehouse:message code="product.description.label"/>
                                            </th>
                                            <th class="center middle">
                                                <warehouse:message code="default.lotNumber.label" default="Lot"/>
                                            </th>
                                            <th class="center middle">
                                                <warehouse:message code="default.expDate.label" default="Exp Date"/>
                                            </th>
                                            <th class="center middle">
                                                <warehouse:message code="default.qty.label"/>
                                            </th>
                                            <th class="center middle">
                                                <warehouse:message code="default.uom.label"/>
                                            </th>
                                            <th class="center middle">
                                                <warehouse:message code="default.bins.label" default="Bins"/>
                                            </th>
                                            <th class="center">
                                                <g:message code="shipmentItem.binLocationPicked.label" default="Bin Picked"/>
                                            </th>
                                            <th class="center">
                                                <g:message code="shipmentItem.lotNumberPicked.label" default="Lot Picked"/>
                                            </th>
                                            <th class="center">
                                                <g:message code="shipmentItem.quantityPicked.label" default="Qty Picked"/>
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

                                                    <g:if test="${entry?.shipmentItem?.inventoryItem?.product?.coldChain }">
                                                            <img src="${resource(dir: 'images/icons', file: 'coldchain.gif')}"
                                                                 alt="" title="${warehouse.message(code:'product.coldChain.message') }" class="middle"/>
                                                    </g:if>
                                                </td>
                                                <td>
                                                    ${entry?.shipmentItem?.inventoryItem?.lotNumber?:entry?.shipmentItem?.lotNumber  }
                                                </td>
                                                <td>
                                                    <format:expirationDate obj="${entry?.shipmentItem?.inventoryItem?.expirationDate?:entry?.shipmentItem?.expirationDate }"/>
                                                </td>
                                                <td class="center">
                                                    ${entry?.shipmentItem?.quantity }
                                                </td>
                                                <td class="border-right">
                                                    ${entry?.shipmentItem?.inventoryItem?.product?.unitOfMeasure?:entry?.shipmentItem?.product?.unitOfMeasure}
                                                </td>
                                                <td class="border-right" style="padding:0;margin:0">
                                                    <table>
                                                        <g:each var="binLocationEntry" in="${binLocations[entry?.shipmentItem?.inventoryItem]}">
                                                            <tr>
                                                                <td>
                                                                    <label>${binLocationEntry?.binLocation?.name?:g.message(code:'default.label')}:</label>
                                                                </td>
                                                                <td>
                                                                    ${binLocationEntry?.quantity}
                                                                </td>
                                                            </tr>
                                                        </g:each>
                                                    </table>
                                                </td>
                                                <td class="border-right" width="10%">
                                                    <g:if test="${entry?.shipmentItem?.binLocation}">
                                                        ${entry?.shipmentItem?.binLocation?.name}
                                                    </g:if>
                                                </td>
                                                <td class="border-right" width="10%">

                                                </td>
                                                <td class="border-right" width="10%">

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
