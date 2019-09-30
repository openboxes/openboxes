<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'shipping.shipment.label', default: 'Shipment').toLowerCase()}" />
	<title><warehouse:message code="default.view.label" args="[entityName]" /></title>        
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>
</head>

<body>    
	<div class="body">

        <g:render template="summary"/>

        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <g:hasErrors bean="${shipmentInstance}">
            <div class="errors">
                <g:renderErrors bean="${shipmentInstance}" as="list" />
            </div>
        </g:hasErrors>

        <div class="buttons">
            <div class="button-container" style="text-align: left">
                <g:render template="../shipment/actions" model="[shipmentInstance:shipmentInstance]" />
                <g:render template="buttons"/>
            </div>
        </div>

		<div class="dialog">

			<div class="yui-gf">

                <div class="yui-u first">

					<div id="details" class="box">
						<h2>
							<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="Details" style="vertical-align: middle"/>
							<label><warehouse:message code="shipping.details.label"/></label>
						</h2>
						<table>
							<tbody>
							<tr class="prop">
								<td valign="top" class="name">
									<label><warehouse:message code="default.status.label" /></label>
								</td>
								<td valign="top" id="shipmentStatus" class="value">
									<format:metadata obj="${shipmentInstance?.currentStatus}"/>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
									<g:if test="${shipmentInstance?.status.code in [org.pih.warehouse.shipping.ShipmentStatusCode.SHIPPED, org.pih.warehouse.shipping.ShipmentStatusCode.RECEIVED]}">
										<label><warehouse:message code="shipping.departed.label" /></label>
									</g:if>
									<g:else>
										<label><warehouse:message code="shipping.departing.label"/></label>
									</g:else>
								</td>
								<td valign="top" class="value">
									<span class="location" id="shipmentOrigin">
										${fieldValue(bean: shipmentInstance, field: "origin.name")}
									</span>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
									<g:if test="${shipmentInstance?.status?.code == org.pih.warehouse.shipping.ShipmentStatusCode.RECEIVED}">
										<label><warehouse:message code="shipping.arrived.label" /></label>
									</g:if>
									<g:else>
										<label><warehouse:message code="shipping.arriving.label" /></label>
									</g:else>
								</td>
								<td valign="top" class="value">
									<span id="shipmentDestination">
										${fieldValue(bean: shipmentInstance, field: "destination.name")}
									</span>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
									<label><warehouse:message
											code="shipping.totalWeight.label" /></label>
								</td>
								<td valign="top" class="value">
									<g:formatNumber format="#,##0.00" number="${shipmentInstance?.totalWeightInPounds() ? shipmentInstance?.totalWeightInPounds() : 0.00 }" />
									<warehouse:message code="default.lbs.label"/>
								</td>
							</tr>
							<g:if test="${!shipmentWorkflow?.isExcluded('totalValue')||!shipmentWorkflow?.isExcluded('statedValue')}">
								<g:if test="${!shipmentWorkflow?.isExcluded('totalValue')}">
									<tr class="prop">
										<td valign="top" class="name">
											<label><warehouse:message code="shipping.totalValue.label" /></label><br/>
										</td>
										<td valign="top" class="value">
											<g:formatNumber format="###,###,##0.00" number="${shipmentInstance?.totalValue ? shipmentInstance?.totalValue : 0.00 }" />
											${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
										</td>
									</tr>
								</g:if>
							</g:if>

							<g:if test="${!shipmentWorkflow?.isExcluded('carrier') && shipmentInstance?.carrier}">
								<tr class="prop">
									<td valign="top" class="name">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'user.png')}" alt="carrier" style="vertical-align: middle"/>
										<label><warehouse:message code="shipping.traveler.label" /></label></td>
									<td valign="top" class="value">
										<g:if test="${shipmentInstance?.carrier}">
											${fieldValue(bean: shipmentInstance, field: "carrier.firstName")}
											${fieldValue(bean: shipmentInstance, field: "carrier.lastName")}
										</g:if>
									</td>
								</tr>
							</g:if>


							<g:if test="${!shipmentWorkflow?.isExcluded('additionalInformation') && shipmentInstance?.additionalInformation}">
								<tr class="prop">
									<td valign="top" class="name">
										<label><warehouse:message code="shipping.additionalInformation.label" default="Additional information" /></label><br/>
									</td>
									<td valign="top" class="value">
										<g:if test="${shipmentInstance.additionalInformation}">
											${shipmentInstance.additionalInformation}<br/>
										</g:if>
									</td>
								</tr>
							</g:if>
							<g:if test="${shipmentWorkflow?.documentTemplate}">
								<tr class="prop">
									<td valign="top" class="name">
										<label><warehouse:message code="shipping.templates.label" /></label><br/>
									</td>
									<td valign="top" class="value">
										<a href="${createLink(controller: "shipment", action: "generateDocuments", id: shipmentInstance.id)}">
											<warehouse:message code="shipping.generateDocuments.label" args="[format.metadata(obj:shipmentWorkflow.shipmentType)]"/></a>
									</td>
								</tr>
							</g:if>
							<g:if test="${shipmentWorkflow?.referenceNumberTypes && shipmentInstance?.referenceNumbers}">
								<g:each var="referenceNumberType" in="${shipmentWorkflow?.referenceNumberTypes}" status="i">
									<tr class="prop">
										<!-- list all the reference numbers valid for this workflow -->
										<td valign="top" class="name">
											<label>
												<format:metadata obj="${referenceNumberType}"/>
											</label>
										</td>
										<td valign="top" class="value">
											<g:findAll in="${shipmentInstance?.referenceNumbers}" expr="it.referenceNumberType.id == referenceNumberType.id">
												${it.identifier }
											</g:findAll>
										</td>
									</tr>
								</g:each>
							</g:if>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message
                                                code="shipping.comments.label" default="Comments"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:if test="${!shipmentInstance?.comments}">
                                            <div class="fade"><g:message code="default.noComments.label"/></div>
                                        </g:if>
                                        <g:each var="comment" in="${shipmentInstance?.comments}">
                                            <div class="comment">
                                                <b>${comment?.sender?.name}</b> ·
                                                <span class="fade">
                                                    <g:prettyDateFormat date="${comment?.dateCreated }"/>
                                                </span>
                                                <blockquote>
                                                    <g:if test="${comment?.recipient}">
                                                        <span class="fade" title="${comment?.recipient?.name}">@${comment?.recipient?.username}</span>
                                                    </g:if>
                                                    ${comment?.comment}
                                                </blockquote>
                                            </div>
                                        </g:each>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message
                                                code="shipping.comments.label" default="Documents"/></label>
                                    </td>
                                    <td valign="top" class="value" style="padding: 0; margin: 0">
                                        <table style="width: 100%;" >
                                            <tbody>
                                            <tr class="odd">
                                                <td>
                                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'text_list_numbers.png')}" class="middle"/>
                                                </td>
                                                <td>
                                                    <g:link target="_blank" controller="report" action="printPickListReport" params="['shipment.id':shipmentInstance?.id]">
                                                        <label><g:message code="shipping.picklist.label" default="Pick List"/></label></g:link>

                                                </td>
                                            </tr>
                                            <tr class="even">
                                                <td>
                                                    <img src="${createLinkTo(dir:'images/icons',file:'pdf.png')}" class="middle"/>
                                                </td>
                                                <td>
                                                    <g:link target="_blank" controller="report" action="printShippingReport" params="['shipment.id':shipmentInstance?.id]">
                                                        <label><g:message code="shipping.packingList.label"/></label></g:link>

                                                </td>
                                            </tr>
                                            <tr class="odd">
                                                <td>
                                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'page_break.png')}" class="middle"/>
                                                </td>
                                                <td>
                                                    <g:link target="_blank" controller="report" action="printPaginatedPackingListReport" params="['shipment.id':shipmentInstance?.id]">
                                                        <label><g:message code="shipping.paginatedPackingList.label" default="Packing List (paginated)"/></label>
                                                    </g:link>

                                                </td>
                                            </tr>
                                            <tr class="even">
                                                <td>
                                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}" class="middle"/>
                                                </td>
                                                <td>
                                                    <g:link controller="doc4j" action="downloadPackingList" id="${shipmentInstance?.id }">
                                                        <label><g:message code="shipping.goodsReceiptNote.label" default="Goods Receipt Note (Excel)"/></label>
                                                    </g:link>

                                                </td>
                                            </tr>
                                            <g:each in="${shipmentInstance.documents + shipmentWorkflow.documentTemplates}" var="document" status="i">
                                                <tr id="document-${document.id}" class="${i%2==0?'even':'odd'}">
                                                    <td class="middle">
                                                        <g:set var="f" value="${document?.filename?.toLowerCase()}"/>
                                                        <g:if test="${f.endsWith('.jpg')||f.endsWith('.png')||f.endsWith('.gif') }">
                                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'picture.png')}"/>
                                                        </g:if>
                                                        <g:elseif test="${f.endsWith('.pdf') }">
                                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_acrobat.png')}"/>
                                                        </g:elseif>
                                                        <g:elseif test="${f.endsWith('.doc')||f.endsWith('.docx') }">
                                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_word.png')}"/>
                                                        </g:elseif>
                                                        <g:elseif test="${f.endsWith('.xls')||f.endsWith('.xlsx')||f.endsWith('.csv') }">
                                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}"/>
                                                        </g:elseif>
                                                        <g:elseif test="${f.endsWith('.gz')||f.endsWith('.jar')||f.endsWith('.zip')||f.endsWith('.tar') }">
                                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_compressed.png')}"/>
                                                        </g:elseif>
                                                        <g:else>
                                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white.png')}"/>
                                                        </g:else>
                                                    </td>
                                                    <td class="middle">
                                                        <g:link controller="document" action="download" id="${document.id}" params="[shipmentId:shipmentInstance?.id]" title="${document?.filename}">
                                                            <label><format:metadata obj="${document?.name}"/></label>
                                                        </g:link>
                                                    </td>

                                                </tr>

                                            </g:each>

                                            </tbody>
                                        </table>
                                    </td>
                                </tr>
							</tbody>
						</table>
                    </div>
                    <div class="box">
                        <h2>
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'clock.png')}" alt="Lead Time" style="vertical-align: middle"/>
                            <g:message code="shipping.leadtime.label" default="Lead Time"/>
                        </h2>
                        <table>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label><warehouse:message
                                            code="shipping.timeToProcess.label" default="Time to process" /></label>
                                </td>
                                <td valign="top" class="value">
                                    <g:relativeTime timeDuration="${shipmentInstance?.timeToProcess()}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label><warehouse:message
                                            code="shipping.timeInCustoms.label" default="Time in customs"/></label>
                                </td>
                                <td valign="top" class="value">
                                    <g:relativeTime timeDuration="${shipmentInstance?.timeInCustoms()}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label><warehouse:message
                                            code="shipping.timeInTransit.label" default="Time in transit"/></label>
                                </td>
                                <td valign="top" class="value">
                                    <g:relativeTime timeDuration="${shipmentInstance?.timeInTransit()}"/>
                                </td>
                            </tr>

                        </table>

					</div>
				</div>
				<div class="yui-u">
                    <div id="shipment-details-tabs" class="tabs">
                        <ul>
                            <li><a href="#details-tab"><warehouse:message code="shipping.contents.label"/></a></li>
                            <li><a href="#receipt-tab"><warehouse:message code="receipt.label" default="Receipt"/></a></li>
                            <li><a href="#documents-tab"><warehouse:message code="documents.label" default="Documents"/></a></li>
                            <li><a href="#comments-tab"><warehouse:message code="comments.label" default="Comments"/></a></li>
                            <li><a href="#events-tab"><warehouse:message code="events.label" default="Comments"/></a></li>
                            <li>
                                <a href="${request.contextPath}/shipment/showTransactions/${shipmentInstance?.id}">
                                    <warehouse:message code="transactions.label" default="Transactions"/>
                                </a>
                            </li>
                        </ul>
                        <div id="details-tab">

                            <g:set var="shipmentItemsByContainer" value="${shipmentInstance?.shipmentItems?.groupBy { it.container } }"/>
                            <div id="items" class="box">
                                <h2>
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="contents" style="vertical-align: middle"/>
                                    <label><warehouse:message code="shipping.contents.label"/></label>
                                </h2>
                                <table>
                                    <tr>
                                        <th><warehouse:message code="shipping.container.label"/></th>
                                        <th><warehouse:message code="product.productCode.label"/></th>
                                        <th><warehouse:message code="product.label"/></th>
                                        <th class="left">
                                            <warehouse:message code="receiptItem.binLocation.label" default="Bin Location"/>
                                            <g:if test="${shipmentInstance?.origin?.id == session.warehouse?.id}">
                                                <small><warehouse:message code="location.picking.label"/></small>
                                            </g:if>
                                            <g:elseif test="${shipmentInstance?.destination?.id == session.warehouse?.id}">
                                                <small><warehouse:message code="location.putaway.label"/></small>
                                            </g:elseif>
                                        </th>
                                        <th class="left"><warehouse:message code="default.lotSerialNo.label"/></th>
                                        <th class="center"><warehouse:message code="default.expires.label"/></th>
                                        <th class="center"><warehouse:message code="shipping.shipped.label"/></th>
                                        <g:if test="${shipmentInstance?.wasReceived()}">
                                            <th class="center"><warehouse:message code="shipmentItem.quantityReceived.label" default="Received"/></th>
                                            <th class="center"><warehouse:message code="shipmentItem.quantityCanceled.label" default="Canceled"/></th>
                                        </g:if>
                                        <th><warehouse:message code="product.uom.label"/></th>
                                        <th><warehouse:message code="shipping.recipient.label"/></th>
                                        <th class="left"><warehouse:message code="default.comment.label"/></th>
                                        <th><warehouse:message code="shipmentItem.isFullyReceived.label" default="Received?"/></th>
                                    </tr>
                                    <g:if test="${shipmentInstance.shipmentItems}">
                                        <g:set var="count" value="${0 }"/>
                                        <g:set var="previousContainer"/>
                                        <g:each var="shipmentItem" in="${shipmentInstance.sortShipmentItems()}" status="i">
                                            <g:set var="rowspan" value="${shipmentItemsByContainer[shipmentItem?.container]?.size() }"/>
                                            <g:set var="newContainer" value="${previousContainer != shipmentItem?.container }"/>
                                            <tr class="${(count++ % 2 == 0)?'odd':'even'} ${newContainer?'new-container':''} shipmentItem">
                                                <g:if test="${newContainer }">
                                                    <td class="top left packing-unit" rowspan="${rowspan}">
                                                        <g:render template="container" model="[container:shipmentItem?.container,showDetails:true]"/>
                                                    </td>
                                                </g:if>
                                                <td>
                                                    ${shipmentItem?.inventoryItem?.product?.productCode}
                                                </td>
                                                <td class="product">
                                                    <g:link controller="inventoryItem" action="showStockCard" id="${shipmentItem?.inventoryItem?.product?.id}">
                                                        <format:product product="${shipmentItem?.inventoryItem?.product}"/>
                                                    </g:link>
                                                </td>
                                                <td>
                                                    <g:if test="${shipmentInstance?.origin?.id == session.warehouse?.id}">
                                                        <g:if test="${shipmentItem?.binLocation}">
                                                            ${shipmentItem?.binLocation?.name}
                                                        </g:if>
                                                        <g:else>
                                                            ${g.message(code:'default.label')}
                                                        </g:else>
                                                    </g:if>
                                                    <g:elseif test="${shipmentInstance?.destination?.id == session?.warehouse?.id}">
                                                        <g:if test="${shipmentItem?.receiptItems}">
                                                            <g:each var="receiptItem" in="${shipmentItem?.receiptItems}">
                                                                <div style="margin: 10px;" title="${receiptItem?.quantityReceived} ${receiptItem?.inventoryItem?.product?.unitOfMeasure?:'EA'}">
                                                                    ${receiptItem?.binLocation?.name?:g.message(code:'default.label')}
                                                                </div>
                                                            </g:each>
                                                        </g:if>
                                                    </g:elseif>

                                                </td>
                                                <td class="lotNumber">
                                                    ${shipmentItem?.inventoryItem?.lotNumber}
                                                </td>
                                                <td class="center expirationDate">

                                                    <g:if test="${shipmentItem?.inventoryItem?.expirationDate}">
                                                        <span class="expirationDate">
                                                            <g:formatDate date="${shipmentItem?.inventoryItem?.expirationDate}" format="d MMM yyyy"/>
                                                        </span>
                                                    </g:if>
                                                    <g:else>
                                                        <span class="fade">
                                                            ${warehouse.message(code: 'default.never.label')}
                                                        </span>
                                                    </g:else>

                                                </td>
                                                <td class="center quantity">
                                                    <g:formatNumber number="${shipmentItem?.quantity}" format="###,##0" />

                                                </td>
                                                <g:if test="${shipmentInstance?.wasReceived()}">
                                                    <td class="center" style="white-space:nowrap;${shipmentItem?.quantityReceived() != shipmentItem?.quantity ? ' color:red;' : ''}">
                                                        <g:formatNumber number="${shipmentItem?.quantityReceived()}" format="###,##0"/>
                                                    </td>
                                                    <td class="center" style="white-space:nowrap;${shipmentItem?.quantityReceived() != shipmentItem?.quantity ? ' color:red;' : ''}">
                                                        <g:formatNumber number="${shipmentItem?.quantityCanceled()}" format="###,##0"/>
                                                    </td>

                                                </g:if>
                                                <td>
                                                    ${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                                                </td>

                                                <td class="left">
                                                    <g:if test="${shipmentItem?.recipient }">
                                                        <div title="${shipmentItem?.recipient?.email}">${shipmentItem?.recipient?.name}</div>
                                                    </g:if>
                                                    <g:else>
                                                        <div class="fade"><g:message code="default.none.label"/></div>
                                                    </g:else>
                                                </td>
                                                <td class="left" >
                                                    <g:if test="${shipmentItem?.comments}">
                                                        <div title="${shipmentItem?.comments.join("<br/>")}">
                                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'note.png')}" />
                                                        </div>
                                                    </g:if>
                                                    <g:else>
                                                        <div class="fade"><g:message code="default.empty.label"/></div>
                                                    </g:else>
                                                </td>
                                                <td>
                                                    ${shipmentItem?.isFullyReceived()}
                                                </td>
                                            </tr>
                                            <g:set var="previousContainer" value="${shipmentItem.container }"/>
                                        </g:each>
                                    </g:if>
                                    <g:else>
                                        <tr>
                                            <td colspan="9" class="middle center fade empty">
                                                <warehouse:message code="shipment.noShipmentItems.message"/>
                                            </td>
                                        </tr>
                                    </g:else>

                                </table>
                            </div>
                        </div>
                        <div id="receipt-tab">
                            <div id="receipt" class="box">
                                <h2>
                                    <img src="${createLinkTo(dir:'images/icons',file:'handtruck.png')}" />
                                    <label><warehouse:message code="shipping.receipt.label"/></label>
                                </h2>
                                <table>
                                    <tr>
                                        <th><g:message code="product.productCode.label"/></th>
                                        <th><g:message code="product.label"/></th>
                                        <th><g:message code="location.binLocation.label"/></th>
                                        <th><g:message code="inventoryItem.lotNumber.label"/></th>
                                        <th><g:message code="inventoryItem.expirationDate.label"/></th>
                                        <th><g:message code="receiptItem.quantityShipped.label" default="Shipped"/></th>
                                        <th><g:message code="receiptItem.quantityReceived.label" default="Received"/></th>
                                        <th><g:message code="receiptItem.quantityCanceled.label" default="Canceled"/></th>
                                    </tr>

                                    <g:each var="receiptItem" in="${shipmentInstance?.receipt?.receiptItems?.sort()}" status="status">
                                        <tr class="prop ${status%2?'even':'odd'}">
                                            <td>
                                                ${receiptItem?.product.productCode}
                                            </td>
                                            <td>
                                                <format:product product="${receiptItem?.product}"/>
                                            </td>
                                            <td>
                                                ${receiptItem?.binLocation?.name}
                                            </td>
                                            <td>
                                                ${receiptItem?.inventoryItem?.lotNumber}
                                            </td>
                                            <td>
                                                <g:expirationDate date="${receiptItem?.inventoryItem?.expirationDate}"/>
                                            </td>
                                            <td>
                                                ${receiptItem?.quantityShipped?:0}
                                            </td>
                                            <td>
                                                ${receiptItem?.quantityReceived?:0}
                                            </td>
                                            <td>
                                                ${receiptItem?.quantityCanceled?:0}
                                            </td>
                                        </tr>
                                    </g:each>
                                </table>
                                <g:unless test="${shipmentInstance?.receipt || shipmentInstance?.wasReceived()}">
                                    <div class="empty fade center">
                                        <g:message code="shipment.noReceipt.message" default="Shipment has not been received yet"/>
                                    </div>
                                </g:unless>

                            </div>
                        </div>
                        <div id="documents-tab">
                            <div id="documents" class="box">
                                <h2>
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="document" style="vertical-align: middle"/>
                                    <label><warehouse:message code="document.documents.label"/></label>
                                </h2>
                                <table style="width: auto;">
                                    <tbody>
                                    <tr>
                                        <th width="1%"></th>
                                        <th><warehouse:message code="document.label"/></th>
                                        <th class="right"></th>
                                    </tr>

                                    <tr class="odd">
                                        <td>
                                            <img src="${createLinkTo(dir:'images/icons',file:'pdf.png')}" class="middle"/>
                                        </td>
                                        <td>
                                            <div>
                                                <g:link target="_blank" controller="report" action="printShippingReport" params="['shipment.id':shipmentInstance?.id]">
                                                    <label><g:message code="shipping.packingList.label"/></label></g:link>
                                            </div>

                                        </td>
                                        <td class="right">
                                            <g:link target="_blank" controller="report" action="printShippingReport" params="['shipment.id':shipmentInstance?.id]" class="button">
                                                <warehouse:message code="default.button.download.label"/>
                                            </g:link>
                                        </td>
                                    </tr>
                                    <tr class="even">
                                        <td>
                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_break.png')}" class="middle"/>
                                        </td>
                                        <td>
                                            <div>
                                                <g:link target="_blank" controller="report" action="printPaginatedPackingListReport" params="['shipment.id':shipmentInstance?.id]">
                                                    <label><g:message code="shipping.paginatedPackingList.label" default="Packing List (paginated)"/></label>
                                                </g:link>
                                            </div>

                                        </td>
                                        <td class="right">
                                            <g:link target="_blank" controller="report" action="printPaginatedPackingListReport" params="['shipment.id':shipmentInstance?.id]" class="button">
                                                <warehouse:message code="default.button.download.label"/>
                                            </g:link>
                                        </td>
                                    </tr>
                                    <tr class="odd">
                                        <td>
                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}" class="middle"/>
                                        </td>
                                        <td>
                                            <div>
                                                <g:link controller="doc4j" action="downloadPackingList" id="${shipmentInstance?.id }">
                                                    <label><g:message code="shipping.goodsReceiptNote.label" default="Goods Receipt Note (Excel)"/></label>
                                                </g:link>
                                            </div>

                                        </td>
                                        <td class="right">
                                            <g:link controller="doc4j" action="downloadPackingList" id="${shipmentInstance?.id }" class="button">
                                                <warehouse:message code="default.button.download.label"/>
                                            </g:link>

                                        </td>
                                    </tr>
                                    <g:each in="${shipmentInstance.documents + shipmentWorkflow.documentTemplates}" var="document" status="i">
                                        <tr id="document-${document.id}" class="${i%2==0?'odd':'even'}">
                                            <td class="middle">
                                                <g:set var="f" value="${document?.filename?.toLowerCase()}"/>
                                                <g:if test="${f.endsWith('.jpg')||f.endsWith('.png')||f.endsWith('.gif') }">
                                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'picture.png')}"/>
                                                </g:if>
                                                <g:elseif test="${f.endsWith('.pdf') }">
                                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_acrobat.png')}"/>
                                                </g:elseif>
                                                <g:elseif test="${f.endsWith('.doc')||f.endsWith('.docx') }">
                                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_word.png')}"/>
                                                </g:elseif>
                                                <g:elseif test="${f.endsWith('.xls')||f.endsWith('.xlsx')||f.endsWith('.csv') }">
                                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}"/>
                                                </g:elseif>
                                                <g:elseif test="${f.endsWith('.gz')||f.endsWith('.jar')||f.endsWith('.zip')||f.endsWith('.tar') }">
                                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_compressed.png')}"/>
                                                </g:elseif>
                                                <g:else>
                                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white.png')}"/>
                                                </g:else>
                                            </td>
                                            <td class="middle">
                                                <g:link controller="document" action="download" id="${document.id}" params="[shipmentId:shipmentInstance?.id]" title="${document?.filename}">
                                                    <label><format:metadata obj="${document?.name}"/></label>
                                                </g:link>
                                            </td>
                                            <td class="right">

                                                <g:if test="${document?.documentType?.documentCode == org.pih.warehouse.core.DocumentCode.SHIPPING_TEMPLATE}">
                                                    <g:link controller="document" action="edit" id="${document.id}" class="button">
                                                        <warehouse:message code="default.button.edit.label"/>
                                                    </g:link>
                                                </g:if>
                                                <g:else>
                                                    <g:link action="editDocument" params="[documentId:document.id,shipmentId:shipmentInstance.id]" class="button">
                                                        <warehouse:message code="default.button.edit.label"/>
                                                    </g:link>
                                                    <g:link class="button" action="deleteDocument" id="${document?.id}"
                                                            params="[shipmentId:shipmentInstance.id]" onclick="return confirm('${warehouse.message(code:'shipping.confirm.deleteDocument.message')}')">
                                                        <warehouse:message code="default.button.delete.label"/></g:link>

                                                </g:else>
                                                <g:link controller="document" action="download" id="${document.id}" params="[shipmentId:shipmentInstance?.id]" class="button">
                                                    <warehouse:message code="default.button.download.label"/>
                                                </g:link>
                                            </td>
                                        </tr>

                                    </g:each>

                                    </tbody>
                                </table>
                            </div>

                            <div class="box">
                                <h2><g:message code="default.upload.label" args="[g.message(code:'document.label')]" default="Upload a {0}"/></h2>
                                <g:uploadForm controller="document" action="${documentInstance?.id ? 'saveDocument' : 'uploadDocument'}">
                                    <g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
                                    <g:hiddenField name="documentId" value="${documentInstance?.id}" />

                                    <table>
                                        <tr class="prop">
                                            <td width="1%" class="name">
                                                <label><warehouse:message code="document.documentType.label" default="Document type"/></label>
                                            </td>
                                            <td class="value">
                                                <g:select name="typeId" from="${org.pih.warehouse.core.DocumentType.list().sort()}"
                                                          noSelection="['null':'Choose a document type']"
                                                          value="${documentInstance?.documentType?.id}"
                                                          optionKey="id" optionValue="${{format.metadata(obj:it)}}"
                                                          class="chzn-select-deselect"/>

                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td class="name">
                                                <label><warehouse:message code="document.label" default="Document"/></label>
                                            </td>
                                            <td class="value">
                                                <input name="fileContents" type="file" />
                                                <button type="submit" class="button icon arrowup right">${documentInstance?.id ? warehouse.message(code:'default.button.save.label') : warehouse.message(code:'default.button.upload.label')}</button>
                                            </td>

                                        </tr>
                                    </table>

                                </g:uploadForm>
                            </div>
                        </div>
                        <div id="comments-tab">
                            <div id="comments" class="box">
                                <h2>
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'comment.png')}" alt="note" style="vertical-align: middle"/>
                                    <label>${warehouse.message(code: 'comments.label') }</label>
                                </h2>

                                <table>
                                    <tbody>
                                    <g:each in="${shipmentInstance.comments}" var="comment" status="i">
                                        <tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'}" >
                                            <td width="1%">
                                                <div class="nailthumb-container">
                                                    <g:if test="${comment?.sender?.photo}">

                                                        <g:link controller="user" action="viewPhoto" id="${comment?.sender?.id }">
                                                            <img src="${createLink(controller:'user', action:'viewThumb', id:comment?.sender?.id)}"
                                                                 style="vertical-align: middle" />
                                                        </g:link>
                                                    </g:if>
                                                    <g:else>
                                                        <img class="photo" src="${resource(dir: 'images/icons/user', file: 'default-avatar.jpg') }"
                                                             style="vertical-align: bottom;" />
                                                    </g:else>
                                                </div>

                                            </td>
                                            <td>
                                                <b>${comment?.sender?.name}</b> ·
                                                <span class="fade">
                                                    <g:prettyDateFormat date="${comment?.dateCreated }"/>
                                                </span>

                                                <blockquote>
                                                    <g:if test="${comment?.recipient}">
                                                        <span class="fade" title="${comment?.recipient?.name}">@${comment?.recipient?.username}</span>
                                                    </g:if>
                                                    ${comment?.comment}
                                                </blockquote>

                                            </td>
                                            <td>
                                                <p class="fade right"></p>
                                            </td>
                                        </tr>

                                    </g:each>

                                    <tr class="">
                                        <td colspan="3" class="left">
                                            <g:form action="saveComment">
                                                <g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
                                                <div>
                                                    <label><warehouse:message code="comment.label" default="Comments"/></label>
                                                </div>
                                                <div style="padding:1px;">
                                                    <g:textArea name="comment" rows="5" style="width:100%" placeholder="${warehouse.message(code:'default.comment.message')}" class="text"/>
                                                </div>

                                                <div class="buttons">
                                                    <button type="submit" class="button icon add"><warehouse:message code="default.button.add.label"/></button>
                                                </div>
                                            </g:form>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        <div id="events-tab">
                            <div id="events" class="box">
                                <h2>
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'date.png')}" alt="event" style="vertical-align: middle"/>
                                    <label><warehouse:message code="shipping.tracking.label"/></label>
                                </h2>
                                <g:set var="i" value="${0 }"/>
                                <table>
                                    <thead>
                                    <tr>
                                        <th><warehouse:message code="default.event.label"/></th>
                                        <th><warehouse:message code="default.date.label"/></th>
                                        <th><warehouse:message code="default.time.label"/></th>
                                        <th><warehouse:message code="location.label"/></th>
                                        <th></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr class="${(i++ % 2) == 0 ? 'odd' : 'even'}">
                                        <td>
                                            <warehouse:message code="default.created.label"/>
                                        </td>
                                        <td>
                                            <g:formatDate date="${shipmentInstance?.dateCreated}" format="MMM d, yyyy"/>
                                        </td>
                                        <td>
                                            <g:formatDate date="${shipmentInstance?.dateCreated}" format="hh:mma"/>
                                        </td>
                                        <td>
                                            ${shipmentInstance?.origin?.name}
                                        </td>
                                        <td style="text-align: right">

                                        </td>
                                    </tr>
                                    <g:each in="${shipmentInstance?.events?.sort { it?.eventDate}}" var="event">
                                        <tr class="${(i++ % 2) == 0 ? 'odd' : 'even'}">
                                            <td>
                                                <g:link action="editEvent" id="${event?.id}" params="[shipmentId:shipmentInstance.id]">
                                                    <format:metadata obj="${event?.eventType}"/>
                                                </g:link>

                                            </td>
                                            <td>
                                                <g:formatDate date="${event.eventDate}" format="MMM d, yyyy"/>
                                            </td>
                                            <td>
                                                <g:formatDate date="${event.eventDate}" format="hh:mma"/>
                                            </td>

                                            <td>
                                                ${event?.eventLocation?.name}
                                            </td>
                                            <td style="text-align: right">
                                                <g:if test="${event?.eventType?.eventCode == org.pih.warehouse.core.EventCode.SHIPPED }">

                                                </g:if>
                                                <g:if test="${event?.eventType?.eventCode == org.pih.warehouse.core.EventCode.RECEIVED }">

                                                </g:if>
                                            </td>
                                        </tr>
                                    </g:each>
                                    </tbody>
                                </table>
                            </div>
                            <div class="box">
                                <h2>
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="event" style="vertical-align: middle"/>
                                    <label><warehouse:message code="shipping.addEvent.label" default="Add Event"/></label>
                                </h2>
                                <g:form controller="shipment" action="saveEvent" method="POST">
                                    <g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
                                    <g:hiddenField name="eventId" value="${eventInstance?.id}" />
                                    <table>
                                        <tbody>

                                        <tr class="prop">
                                            <td valign="top" class="name"><label><warehouse:message code="shipping.eventType.label"/></label></td>
                                            <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'eventType', 'errors')}">
                                                <g:if test="${!eventInstance?.eventType}">
                                                    <g:select id="eventType.id" name='eventType.id' noSelection="['':warehouse.message(code:'default.selectOne.label')]"
                                                              from='${org.pih.warehouse.core.EventType.list()}' optionKey="id"
                                                              optionValue="${{format.metadata(obj:it)}}" value="${eventInstance?.eventType}" class="chzn-select-deselect">
                                                    </g:select>
                                                </g:if>
                                                <g:else>
                                                    <g:hiddenField name="eventType.id" value="${eventInstance?.eventType?.id}"/>
                                                    <format:metadata obj="${eventInstance?.eventType}"/>
                                                </g:else>
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td valign="top" class="name"><label><warehouse:message code="location.label" /></label></td>
                                            <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'location', 'errors')}">
                                                <g:select id="eventLocation.id" name='eventLocation.id' noSelection="['':warehouse.message(code:'default.selectOne.label')]"
                                                          from='${org.pih.warehouse.core.Location.list()}' optionKey="id" optionValue="name"
                                                          value="${eventInstance?.eventLocation?.id?:session?.warehouse?.id}" class="chzn-select-deselect">
                                                </g:select>
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td valign="top" class="name"><label><warehouse:message code="shipping.eventDate.label" /></label></td>
                                            <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'eventDate', 'errors')}">
                                                <g:datePicker name="eventDate" value="${eventInstance?.eventDate}" precision="minute"/>
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td class="name"></td>
                                            <td class="value">
                                                <button type="submit" class="button">
                                                    <warehouse:message code="default.button.save.label"/>
                                                </button>
                                            </td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </g:form>

                            </div>
                        </div>


                    </div>



				</div>
			</div>
		</div>
	</div>
    <script>
        $(document).ready(function() {
            $('.nailthumb-container').nailthumb({ width : 48, height : 48 });
            $(".tabs").tabs({
                cookie : {
                    expires : 1
                }
            });
        });
    </script>


</body>
</html>
