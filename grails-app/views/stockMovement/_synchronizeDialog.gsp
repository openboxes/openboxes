<g:form controller="stockMovement" action="synchronize">
    <g:hiddenField name="id" value="${stockMovement?.id}"/>

    <div class="message">
        We will create shipment items and transaction entries for the
        following ${stockMovement?.requisition?.picklist?.picklistItems?.size()} picklist items.
    </div>

    <div class="tabs">
        <ul>
            <li><a href="#tabs-1"><warehouse:message code="stockMovement.label"/></a></li>
            <li><a href="#tabs-2"><warehouse:message code="picklist.picklistItems.label"/></a></li>
        </ul>
        <div id="tabs-1">
            <table>
                <tr class="prop">
                    <td class="name">
                        <label><g:message code="stockMovement.identifier.label"/></label>
                    </td>
                    <td class="value">
                        ${stockMovement?.identifier}
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><g:message code="stockMovement.label"/></label>
                    </td>
                    <td class="value">
                        ${stockMovement?.name}
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><g:message code="stockMovement.origin.label"/></label>
                    </td>
                    <td class="value">
                        ${stockMovement?.origin?.name}
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><g:message code="stockMovement.destination.label"/></label>
                    </td>
                    <td class="value">
                        ${stockMovement?.destination?.name}
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><g:message code="default.dateCreated.label"/></label>
                    </td>
                    <td class="value">
                        <g:formatDate date="${stockMovement?.dateCreated}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><g:message code="stockMovement.dateRequested.label"/></label>
                    </td>
                    <td class="value">
                        <g:formatDate date="${stockMovement?.dateRequested}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><g:message code="default.lastUpdated.label"/></label>
                    </td>
                    <td class="value">
                        <g:formatDate date="${stockMovement?.lastUpdated}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><g:message code="stockMovement.dateShipped.label"/></label>
                    </td>
                    <td class="value">
                        <g:datePicker name="dateShipped" value="${stockMovement.dateShipped}" default="none" noSelection="['':'']"/>
                    </td>
                </tr>
            </table>
        </div>
        <div id="tabs-2">
            <g:render template="/common/dataTable" model="[data: data]"/>
        </div>
    </div>
        <tfoot>
        <tr>
            <td colspan="2">
                <div class="buttons">
                    <button class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}" />&nbsp;
                        <g:message code="default.button.synchronize.label" default="Synchronize"/>
                    </button>
                </div>
            </td>
        </tr>
        </tfoot>
    </table>
</g:form>
<g:javascript>
$(document).ready(function() {
    $(".tabs").tabs();
});
</g:javascript>
