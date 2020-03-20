<%@ page import="org.pih.warehouse.core.RoleType" %>
<!-- Only allow the originating warehouse to edit the shipment -->

<g:if test="${shipmentInstance?.destination?.id == session.warehouse.id}">
    <g:link controller="shipment" action="list" params="[type: 'incoming']" class="button">
        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'lorry_stop.png')}" class="middle"/>&nbsp;
        <warehouse:message code="shipping.listIncoming.label"/>
    </g:link>
</g:if>
<g:else>
    <g:link controller="shipment" action="list" class="button">
        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'lorry_start.png')}" class="middle"/>&nbsp;
        <warehouse:message code="shipping.listOutgoing.label"/>
    </g:link>
</g:else>
<g:if test="${actionName != 'showDetails'}">
    <g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}" class="button">
        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'zoom.png')}" class="middle"/>&nbsp;
        <g:if test="${request.request.requestURL.toString().contains('showDetails')}"><warehouse:message
                code="shipping.showDetails.label"/></g:if>
        <g:else><warehouse:message code="shipping.showDetails.label"/></g:else>
    </g:link>
</g:if>

<g:if test="${shipmentInstance.hasShipped()}">
    <g:isUserInRole roles="[org.pih.warehouse.core.RoleType.ROLE_ADMIN]">
        <g:link controller="createShipmentWorkflow" action="createShipment" id="${shipmentInstance.id}" class="button">
            <img src="${createLinkTo(dir: 'images/icons/silk', file: 'pencil.png')}" class="middle"/>&nbsp;
            <g:if test="${request.request.requestURL.toString().contains('createShipment')}">
                <warehouse:message code="shipping.editShipment.label"/>
            </g:if>
            <g:else><warehouse:message code="shipping.editShipment.label"/></g:else>
        </g:link>
    </g:isUserInRole>
</g:if>

<g:if test="${!shipmentInstance.hasShipped()}">
    <!-- you can only edit a shipment or its packing list if you are at the origin warehouse, or if the origin is not a warehouse, and you are at the destination warehouse -->
    <g:if test="${shipmentInstance?.origin?.id == session?.warehouse?.id || shipmentInstance?.destination?.id == session?.warehouse?.id}">
        <div class="button-group">
            <g:link controller="createShipmentWorkflow" action="createShipment" id="${shipmentInstance.id}" class="button">
                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'pencil.png')}" class="middle"/>&nbsp;
                <g:if test="${request.request.requestURL.toString().contains('createShipment')}"><warehouse:message
                        code="shipping.editShipment.label"/></g:if>
                <g:else><warehouse:message code="shipping.editShipment.label"/></g:else>
            </g:link>
            <g:link controller="createShipmentWorkflow" action="createShipment" event="enterTrackingDetails"
                    id="${shipmentInstance.id}" class="button">
                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'map.png')}" class="middle"/>&nbsp;
                <warehouse:message code="shipping.enterTrackingDetails.label"/>
            </g:link>
            <g:link controller="createShipmentWorkflow" action="createShipment" event="enterContainerDetails"
                    id="${shipmentInstance?.id}" params="[skipTo: 'Packing']" class="button">
                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'package_add.png')}"
                     class="middle"/>&nbsp;
                <warehouse:message code="shipping.editPackingList.label"/>
            </g:link>
            <g:link controller="createShipmentWorkflow" action="createShipment" event="enterContainerDetails"
                    id="${shipmentInstance?.id}" params="[skipTo: 'Picking']" class="button">
                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'basket_put.png')}"
                     class="middle"/>&nbsp;
                <warehouse:message code="shipping.pickShipmentItems.label"/>
            </g:link>
            <g:if test="${shipmentInstance?.isSendAllowed()}">
                <g:link controller="createShipmentWorkflow" action="createShipment" event="sendShipment"
                        id="${shipmentInstance.id}" params="[skipTo: 'Sending']" class="button">
                    <img src="${createLinkTo(dir: 'images/icons', file: 'truck.png')}" class="middle"/>&nbsp;
                    <warehouse:message code="shipping.sendShipment.label"/>
                </g:link>
            </g:if>
            <g:else>
                <g:set var="message" value="Shipment cannot be sent yet"/>
                <g:if test="${shipmentInstance?.hasShipped()}">
                    <g:set var="message" value="Shipment has already been shipped!"/>
                </g:if>
                <g:elseif test="${shipmentInstance?.wasReceived()}">
                    <g:set var="message" value="Shipment has already been received!"/>
                </g:elseif>

                <a href="javascript:void(0);" onclick="alert('${message}')" class="button">
                    <img src="${createLinkTo(dir: 'images/icons/silk', file: 'lorry.png')}"
                         class="middle"/>&nbsp;
                    <span class="fade">
                        <warehouse:message code="shipping.sendShipment.label"/>
                    </span>
                </a>
            </g:else>
        </div>
    </g:if>
</g:if>

<g:isUserManager>

    <div class="button-group">

        <g:if test="${shipmentInstance?.origin?.id == session?.warehouse?.id || shipmentInstance?.destination?.id == session?.warehouse?.id}">

            <g:if test="${shipmentInstance?.isReceiveAllowed()}">
                <g:link controller="shipment" action="receiveShipment" id="${shipmentInstance.id}"
                        name="receiveShipmentLink" class="button">
                    <img src="${createLinkTo(dir: 'images/icons', file: 'handtruck.png')}" alt="Receive Shipment"
                         class="middle"/>&nbsp;
                    <warehouse:message code="shipping.receiveShipment.label"/>
                </g:link>
                <g:if test="${shipmentInstance?.isPartialReceiveAllowed()}">
                    <g:link controller="partialReceiving" action="create" id="${shipmentInstance.id}"
                            name="receiveShipmentLink" class="button">
                        <img src="${createLinkTo(dir: 'images/icons', file: 'handtruck.png')}"
                             class="middle"/>&nbsp;
                        <warehouse:message code="shipping.partialReceipt.label" default="Partial Receipt"/>
                    </g:link>
                </g:if>

            </g:if>
            <g:else>
                <g:set var="message" value="Shipment cannot be received yet"/>
                <g:if test="${!shipmentInstance?.hasShipped()}">
                    <g:set var="message" value="Shipment has not been shipped!"/>
                </g:if>
                <g:elseif test="${shipmentInstance?.wasReceived()}">
                    <g:set var="message" value="Shipment was already received!"/>
                </g:elseif>
                <a href="javascript:void(0);" onclick="alert('${message}')" class="button">
                    <img src="${createLinkTo(dir: 'images/icons', file: 'handtruck.png')}"
                         alt="Receive Shipment" class="middle"/>&nbsp;
                    <span class="fade"><warehouse:message code="shipping.receiveShipment.label"/></span>
                </a>
            </g:else>
        </g:if>
    </div>
</g:isUserManager>

<g:isUserInRole roles="[org.pih.warehouse.core.RoleType.ROLE_SUPERUSER]">
    <g:if test="${shipmentInstance?.hasShipped()}">
        <g:link controller="shipment" action="rollbackLastEvent" id="${shipmentInstance?.id}" class="button">
            <img src="${createLinkTo(dir: 'images/icons/silk', file: 'arrow_undo.png')}"
                 alt="Rollback Last Event" class="middle"/>&nbsp;
            <warehouse:message code="shipping.rollbackLastEvent.label"/></g:link>
    </g:if>
</g:isUserInRole>

<g:if test="${shipmentInstance.requisition}">
    <g:link controller="stockMovement" action="edit" id="${shipmentInstance?.requisition?.id}" class="button">
        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'package.png')}" class="middle"/>&nbsp;
        <warehouse:message code="stockMovement.edit.label" default="Edit stock movement"/>
    </g:link>
</g:if>



<div class="right">

    <div class="action-menu">
        <div class="action-btn">
            <div class="button">
                <img src="${createLinkTo(dir: 'images/icons', file: 'pdf.png')}" class="middle"/>&nbsp; <g:message code="default.button.download.label"/>

            </div>
        </div>
        <div class="actions">
            <div class="action-menu-item">
                <g:link target="_blank" controller="report" action="printPickListReport"
                        params="['shipment.id': shipmentInstance?.id]">
                    <img src="${createLinkTo(dir: 'images/icons/silk', file: 'application_side_list.png')}" class="middle"/>&nbsp;
                    <warehouse:message code="shipping.printPickList.label" default="Print Pick List"/>
                </g:link>
            </div>
            <div class="action-menu-item">
                <g:link target="_blank" controller="report" action="printShippingReport"
                        params="['shipment.id': shipmentInstance?.id]">
                    <img src="${createLinkTo(dir: 'images/icons', file: 'pdf.png')}" class="middle"/>&nbsp;
                    <warehouse:message code="shipping.printPackingList.label" default="Print Packing List"/>
                </g:link>
            </div>
            <div class="action-menu-item">
                <g:link target="_blank" controller="report" action="printPaginatedPackingListReport"
                        params="['shipment.id': shipmentInstance?.id]">
                    <img src="${createLinkTo(dir: 'images/icons/silk', file: 'page_break.png')}" class="middle"/>&nbsp;
                    <warehouse:message code="shipping.downloadPackingList.label"/>
                    <span class="fade">(.pdf)</span>
                </g:link>
            </div>
            <div class="action-menu-item">
                <g:link controller="doc4j" action="downloadPackingList" id="${shipmentInstance?.id}">
                    <img src="${createLinkTo(dir: 'images/icons/silk', file: 'page_white_excel.png')}"
                         class="middle"/>&nbsp;
                    <warehouse:message code="shipping.downloadPackingList.label"/> <span class="fade">(.xls)</span>
                </g:link>
            </div>

            <div class="action-menu-item">
                <g:link controller="shipment" action="downloadLabels" id="${shipmentInstance?.id}" target="_blank">
                    <img src="${createLinkTo(dir: 'images/icons/', file: 'barcode.png')}"
                         class="middle"/>&nbsp;
                    <warehouse:message code="shipping.downloadBarcodeLabels.label"/>
                </g:link>
            </div>

        </div>
    </div>

</div>
