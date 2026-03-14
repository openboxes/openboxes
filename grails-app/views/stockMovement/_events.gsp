<%@ page import="org.pih.warehouse.core.Constants" %>

<section id="events-tab" class="box" aria-label="Events">
    <style>
        #events-tab table td,
        #events-tab table th {
            padding: 8px 10px;
            line-height: 1.4;
        }
    </style>
    <h2>
        <img src="${resource(dir:'images/icons/silk',file:'date.png')}" alt="event" style="vertical-align: middle"/>
        <label>
            <g:message code="default.shipmentHistory.label" default="Shipment history"/>
        </label>
    </h2>
    <g:if test="${shipmentId}">
        <div class="button-bar">
            <div class="button-group">
                <a href="javascript:void(0);" class="button btn-show-dialog"
                   data-height="400" data-width="800"
                   data-title="${warehouse.message(code:'default.addCustomEvent.label', default: 'Add Custom Event')}"
                   data-url="${request.contextPath}/stockMovement/addCustomEventDialog/${stockMovementId}">
                    <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                    <warehouse:message code="default.addCustomEvent.label" default="Add Custom Event"/>
                </a>
            </div>
        </div>
    </g:if>
    <g:if test="${historyItems.size() > 0}">
        <table>
            <tr>
                <th><g:message code="default.event.label" default="Event"/></th>
                <th><g:message code="default.dateTime.label" default="Date/Time"/></th>
                <th><g:message code="default.location.label" default="Location"/></th>
                <th><g:message code="default.reference.label" default="Reference"/></th>
                <th><g:message code="default.createdBy.label" default="Created by"/></th>
                <th><g:message code="default.comment.label" default="Comment"/></th>
            </tr>
            <g:each var="historyItem" in="${historyItems}" status="status">
                <tr class="${status % 2 ? 'even' : 'odd'}">
                    <td>
                        <strong>${historyItem?.eventType?.name}</strong>
                    </td>
                    <td class="center" nowrap="nowrap">
                        ${historyItem?.date?.format(Constants.EUROPEAN_DATE_FORMAT_WITH_TIME)}
                    </td>
                    <td>
                        <g:if test="${historyItem?.location}">
                            ${historyItem?.location}
                        </g:if>
                        <g:else>
                            <span class="fade"><g:message code="default.none.label" default="None"/></span>
                        </g:else>
                    </td>
                    <td>
                        <g:if test="${historyItem?.referenceDocument?.identifier}">
                            ${historyItem?.referenceDocument?.identifier}
                        </g:if>
                        <g:else>
                            <span class="fade"><g:message code="default.none.label" default="None"/></span>
                        </g:else>
                    </td>
                    <td nowrap="nowrap">
                        <g:if test="${historyItem?.createdBy}">
                            ${historyItem?.createdBy}
                        </g:if>
                        <g:else>
                            <span class="fade"><g:message code="default.none.label" default="None"/></span>
                        </g:else>
                    </td>
                    <td class="left">
                        <g:if test="${historyItem?.comment}">
                            ${historyItem?.comment}
                        </g:if>
                        <g:else>
                            <span class="fade"><g:message code="default.empty.label" default="Empty"/></span>
                        </g:else>
                    </td>
                </tr>
            </g:each>
        </table>
    </g:if>
    <g:else>
        <div class="fade center empty"><g:message code="default.noEvents.label" default="No events"/></div>
    </g:else>
</section>
