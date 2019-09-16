<div class="wizard-box">
	<div class="wizard-steps">
        <g:set var="wizardSteps" value="${['Details':'enterShipmentDetails',
                                           'Tracking':'enterTrackingDetails',
                                           'Packing':'enterContainerDetails',
                                           'Picking':'pickShipmentItems',
                                           'Sending':'sendShipment']}"/>

        <g:each var="wizardStep" in="${wizardSteps}" status="status">

            <g:set var="index" value="${wizardSteps?.keySet()?.findIndexOf{ it == currentState}}"/>

            <g:if test="${index == status}">
                <g:set var="styleClass" value="active-step"/>
            </g:if>
            <g:elseif test="${index > status}">
                <g:set var="styleClass" value="completed-step"/>
            </g:elseif>
            <g:else>
                <g:set var="styleClass" value=""/>
            </g:else>
            <div class="${styleClass}">
                <g:if test="${shipmentInstance?.id}">
                    <g:link controller="createShipmentWorkflow" action="createShipment" event="${wizardStep?.value}" id="${shipmentInstance?.id}">
                        <span>${status+1}</span>
                        <warehouse:message code="shipping.${wizardStep?.value}.label"/>
                    </g:link>
                </g:if>
                <g:else>
                    <a href="#">
                        <span>${status+1}</span>
                        <warehouse:message code="shipping.${wizardStep?.value}.label"/>
                    </a>
                </g:else>
            </div>
        </g:each>
    </div>
    <div class="right">
        <g:if test="${currentState=='Picking'}">
            <g:link target="_blank" controller="report" action="printPickListReport" class="button"
                    params="['shipment.id': shipmentInstance?.id]">
                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'application_view_list.png')}" class="middle"/>&nbsp;
                <warehouse:message code="shipping.printPicklist.label" default="Print Pick List"/>
            </g:link>
        </g:if>
    </div>


</div>