<div id="tracking" class="box">
    <h2>
        <img src="${createLinkTo(dir:'images/icons/silk',file:'map.png')}" />&nbsp;
        <label><warehouse:message code="shipping.tracking.label"/></label>
        <small class="fade">${shipmentInstance?.shipmentMethod?.trackingNumber}</small>
    </h2>

    <div class="buttons left">
        <g:link class="button" controller="createShipmentWorkflow" action="createShipment" event="enterTrackingDetails" id="${shipmentInstance?.id }" params="[skipTo:'Tracking']">
            <img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" />
            <warehouse:message code="default.edit.label" args="[warehouse.message(code: 'shipmentMethod.trackingNumber.label', default: 'Tracking Number')]"/>
        </g:link>
        <g:link url="${trackingUrl}" class="button">
            <img src="${createLinkTo(dir:'images/icons/silk',file:'link.png')}" />
            <warehouse:message code="default.open.label" args="[warehouse.message(code: 'shipmentMethod.trackingNumber.label', default: 'Tracking Number')]"/>
        </g:link>
    </div>
    <g:if test="${trackingUrl}">
        <iframe src="${trackingUrl}" width="100%" height="600"></iframe>
    </g:if>
</div>
