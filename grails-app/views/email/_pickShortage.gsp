<%@ page contentType="text/html"%>
<g:set var="requisition" value="${picklistItem?.requisitionItem?.requisition}"/>
<g:set var="reportedBy" value="${picklistItem?.pickedBy ?: picklistItem?.assignee}"/>
<g:applyLayout name="email">
    <p>
        <strong>Order:</strong> ${requisition?.requestNumber} – ${requisition?.name}<br/>
        <strong>Facility:</strong> ${requisition?.origin?.name}<br/>
        <strong>Product:</strong> ${picklistItem?.inventoryItem?.product?.productCode} – ${picklistItem?.inventoryItem?.product?.name}<br/>
        <strong>Lot:</strong> ${picklistItem?.inventoryItem?.lotNumber}<br/>
        <strong>Bin Location:</strong> ${picklistItem?.binLocation?.name}<br/>
        <strong>Quantity Requested:</strong> ${picklistItem?.quantity}<br/>
        <strong>Quantity Picked:</strong> ${picklistItem?.quantityPicked ?: 0}<br/>
        <strong>Quantity Shorted:</strong> ${picklistItem?.quantityCanceled}<br/>
        <g:if test="${lostAndFound}">
            <strong>Moved to ${lostAndFound.name}:</strong> ${quantityMovedToLostAndFound}<br/>
        </g:if>
        <strong>Shortage Reason:</strong> ${picklistItem?.reasonCode}<br/>
        <strong>Reported By:</strong> ${reportedBy?.firstName} ${reportedBy?.lastName}<br/>
        <strong>Date:</strong> <g:formatDate date="${new Date()}" format="yyyy-MM-dd HH:mm:ss" />
    </p>

    <p>
        Please review and resolve this discrepancy in OpenBoxes:
        <g:link controller="report" action="showLostAndFoundReport" params="[ 'location.id': requisition?.origin?.id ]" absolute="true">
            <g:createLink controller="report" action="showLostAndFoundReport" params="[ 'location.id': requisition?.origin?.id ]" absolute="true" />
        </g:link>
    </p>
</g:applyLayout>
