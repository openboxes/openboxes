<%@ page import="grails.core.GrailsApplication" %>
<%@ page contentType="text/html"%>
<g:applyLayout name="email">

    <g:set var="grailsApplication" value="${grails.util.Holders.findApplication()}" />

    <p>
        <strong>Task ID:</strong> ${putawayTask?.id}<br/>
        <strong>Facility:</strong> ${putawayTask?.facility?.name}<br/>
        <strong>Product:</strong> ${putawayTask?.product?.productCode} – ${putawayTask?.product?.name}<br/>
        <strong>Quantity:</strong> ${putawayTask?.quantity}<br/>
        <strong>Discrepancy Reason:</strong> ${putawayTask?.discrepancyReasonCode}<br/>
        <strong>Reported By:</strong> ${putawayTask?.orderedBy?.firstName} ${putawayTask?.orderedBy?.lastName}<br/>
        <strong>Date/Time:</strong> <g:formatDate date="${putawayTask?.lastUpdated}" format="yyyy-MM-dd HH:mm:ss" />
    </p>

    <p>
        Please review and resolve this discrepancy in OpenBoxes:
        <g:link controller="report" action="showLostAndFoundReport" params="[ 'location.id': putawayTask?.facility?.id ]" absolute="true">
            <g:createLink controller="report" action="showLostAndFoundReport" params="[ 'location.id': putawayTask?.facility?.id ]" absolute="true" />
        </g:link>
    </p>

</g:applyLayout>