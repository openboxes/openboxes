<%@ page import="org.pih.warehouse.core.Constants" %>
<%@ page import="org.pih.warehouse.core.EventType" %>
<%@ page import="org.pih.warehouse.core.Location" %>
<%@ page import="org.pih.warehouse.core.EventCode" %>

<section class="box" aria-label="Events">
    <h2>
        <img src="${resource(dir:'images/icons/silk',file:'date.png')}" alt="event" style="vertical-align: middle"/>
        <label>
            <g:message code="default.shipmentHistory.label" default="Shipment history"/>
        </label>
    </h2>
    <g:if test="${historyItems.size() > 0}">
        <table>
            <thead>
            <tr class="odd">
                <th><g:message code="default.event.label" default="Event"/></th>
                <th><g:message code="default.dateTime.label" default="Date/Time"/></th>
                <th><g:message code="default.location.label" default="Location"/></th>
                <th><g:message code="default.reference.label" default="Reference"/></th>
                <th><g:message code="default.createdBy.label" default="Created by"/></th>
                <th><g:message code="default.comment.label" default="Comment"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each var="historyItem" in="${historyItems}">
                <tr>
                    <td>
                        ${historyItem?.eventType?.name}
                    </td>
                    <td>
                        ${historyItem?.date?.format(Constants.EUROPEAN_DATE_FORMAT_WITH_TIME)}
                    </td>
                    <td>
                        ${historyItem?.location}
                    </td>
                    <td>
                        ${historyItem?.referenceDocument?.identifier}
                    </td>
                    <td>
                        ${historyItem?.createdBy}
                    </td>
                    <td>
                        ${historyItem?.comment}
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:if>
    <g:else>
        <div class="fade center empty"><g:message code="default.noEvents.label" default="No events"/></div>
    </g:else>
</section>

<section class="box">
    <h2>
        <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" alt="event" style="vertical-align: middle"/>
        <label>
            <g:message code="default.addCustomEvent.label" default="Add Custom Event"/>
        </label>
    </h2>
    <g:form controller="shipment" action="saveEvent" method="POST">
        <g:hiddenField name="shipmentId" value="${shipmentId}" />
        <g:hiddenField name="createdBy.id" value="${session.user.id}" />
        <table>
            <tbody>
            %{-- Event type picker --}%
            <tr class="prop">
                <td valign="top" class="name">
                    <label>
                        <g:message code="eventType.label" default="Event type"/>
                    </label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'eventType', 'errors')}">
                <g:select id="eventType.id"
                          name="eventType.id"
                          optionKey="id"
                          noSelection="['': g.message(code: 'default.selectOne.label')]"
                          from='${EventType.listCustomEventTypes()}'
                          optionValue="${{ format.metadata(obj: it) }}"
                          required="${true}"
                          class="chzn-select-deselect">
                </g:select>
                </td>
            </tr>
            %{-- Location picker --}%
            <tr class="prop">
                <td valign="top" class="name">
                    <label>
                        <g:message code="default.location.label" default="Location" />
                    </label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'location', 'errors')}">
                    <g:select id="eventLocation.id"
                              name="eventLocation.id"
                              noSelection="['': warehouse.message(code: 'default.selectOne.label')]"
                              from='${Location.listNonInternalLocations()}'
                              optionKey="id"
                              optionValue="name"
                              class="chzn-select-deselect">
                    </g:select>
                </td>
            </tr>
            %{-- Event date picker --}%
            <tr class="prop">
                <td valign="top" class="name">
                    <label>
                        <g:message code="event.eventDate.label" default="Event date"/>
                    </label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'eventDate', 'errors')}">
                    <g:datePicker name="eventDate" value="${eventInstance?.eventDate}" precision="minute"/>
                </td>
            </tr>
            %{-- Comment input field --}%
            <tr class="prop">
                <td valign="top" class="name">
                    <label>
                        <g:message code="default.comment.label" default="Comment" />
                    </label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'eventDate', 'errors')}">
                    <g:textArea name="comment.comment" cols="100" rows="10" value=""/>
                </td>
            </tr>
            %{-- Save button --}%
            <tr class="prop">
                <td class="name"></td>
                <td class="value">
                    <button type="submit" class="button">
                        <g:message code="default.button.save.label" default="Save"/>
                    </button>
                </td>
            </tr>
            </tbody>
        </table>
    </g:form>
</section>
