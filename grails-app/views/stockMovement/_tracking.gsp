<g:set var="shipmentInstance" value="${stockMovement.shipment}"/>
<div id="tracking-tab">
    <div id="tracking" class="box">
        <h2>
            <img src="${createLinkTo(dir:'images/icons/silk',file:'map.png')}" alt="event" style="vertical-align: middle"/>
            <label><warehouse:message code="shipping.tracking.label"/></label>
        </h2>
        <div id='map' style='width: 100%; height: 600px;'></div>
    </div>
</div>
