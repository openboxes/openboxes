<g:set var="shipmentInstance" value="${stockMovement.shipment}"/>

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
                        <g:link controller="event" action="edit" id="${event?.id}" params="[shipmentId:shipmentInstance.id]">
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
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
</div>
