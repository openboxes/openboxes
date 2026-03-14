<%@ page import="org.pih.warehouse.core.EventType" %>
<%@ page import="org.pih.warehouse.core.Location" %>

<g:form controller="stockMovement" action="saveCustomEvent" method="POST">
    <g:hiddenField name="stockMovementId" value="${stockMovementId}" />
    <g:hiddenField name="createdBy.id" value="${session.user.id}" />
    <table>
        <tbody>
        <tr class="prop">
            <td valign="top" class="name">
                <label><g:message code="eventType.label" default="Event type"/></label>
            </td>
            <td valign="top" class="value">
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
        <tr class="prop">
            <td valign="top" class="name">
                <label><g:message code="default.location.label" default="Location" /></label>
            </td>
            <td valign="top" class="value">
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
        <tr class="prop">
            <td valign="top" class="name">
                <label><g:message code="event.eventDate.label" default="Event date"/></label>
            </td>
            <td valign="top" class="value">
                <g:datePicker name="eventDate"
                              value="${new Date()}"
                              fieldType="${Date}"
                              precision="minute"/>
            </td>
        </tr>
        <tr class="prop">
            <td valign="top" class="name">
                <label><g:message code="default.comment.label" default="Comment" /></label>
            </td>
            <td valign="top" class="value">
                <g:textArea name="comment.comment" cols="80" rows="5" value=""/>
            </td>
        </tr>
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
