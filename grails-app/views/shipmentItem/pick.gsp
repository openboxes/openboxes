<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <%--
    <meta name="layout" content="dialog" />--%>
    <title><g:message code="shipping.pickShipmentItem.label"/></title>
</head>

<body>
<div class="dialog">
    <g:form controller="createShipmentWorkflow" action="createShipment" params="[execution:params.execution]">
        <fieldset>
            <g:hiddenField name="id" value="${shipmentItemInstance?.shipment?.id}" />
            <g:hiddenField name="shipmentItem.id" value="${shipmentItemInstance?.id}" />
            <g:hiddenField name="version" value="${shipmentItemInstance?.version}" />
            <div class="dialog">
                <table>
                    <tbody>
                    <tr class="prop">
                        <td valign="top" class="name">
                            <label for="quantity"><warehouse:message code="shipmentItem.product.label" /></label>
                        </td>
                        <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'product', 'errors')}">
                            ${shipmentItemInstance?.inventoryItem?.product?.productCode}
                            <format:product product="${shipmentItemInstance?.inventoryItem?.product}"/>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td valign="top" class="name">
                            <label for="lotNumber"><warehouse:message code="inventoryItem.lotNumber.label" /></label>
                        </td>
                        <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'lotNumber', 'errors')}">
                            <div id="lotNumber">${shipmentItemInstance?.inventoryItem?.lotNumber}</div>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td valign="top" class="name">
                            <label for="expirationDate"><warehouse:message code="inventoryItem.expirationDate.label" /></label>
                        </td>
                        <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'inventoryItem', 'errors')}">
                            <g:expirationDate id="expirationDate" date="${shipmentItemInstance?.inventoryItem?.expirationDate}"/>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td valign="top" class="name">
                            <label for="quantity"><warehouse:message code="shipmentItem.quantity.label" /></label>
                        </td>
                        <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'quantity', 'errors')}">
                            <g:hiddenField name="quantity" value="${shipmentItemInstance?.quantity}" />
                            ${shipmentItemInstance?.quantity}
                        </td>
                    </tr>
                    <tr class="prop">
                        <td valign="top" class="name">
                            <label for="binLocation"><warehouse:message code="shipmentItem.binLocation.label" /></label>
                        </td>
                        <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'binLocation', 'errors')}">
                            <g:if test="${binLocations}">
                                <table>
                                    <thead>
                                        <tr>
                                            <th></th>
                                            <th><g:message code="default.bin.label" default="Bin"/></th>
                                            <th><g:message code="default.lot.label" default="Lot"/></th>
                                            <th><g:message code="default.exp.label" default="Exp"/></th>
                                            <th><g:message code="default.qty.label" default="Qty"/></th>
                                            <th><g:message code="default.qtyPicked.label" default="Qty Picked"/></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <g:each var="entry" in="${binLocations}" status="status">
                                            <g:set var="statusClass" value="${entry.quantity>=shipmentItemInstance?.quantity?'':'error'}"/>
                                            <g:set var="selected" value="${entry?.binLocation?.id == shipmentItemInstance?.binLocation?.id &&
                                                    entry?.inventoryItem?.id == shipmentItemInstance?.inventoryItem?.id}"/>
                                            <tr class="${statusClass}">
                                                <td class="middle">
                                                    <g:radio name="binLocationAndInventoryItem" value="${entry?.binLocation?.id}:${entry?.inventoryItem?.id}"
                                                             checked="${selected}"/>
                                                </td>
                                                <td class="middle">
                                                    ${entry?.binLocation?.name?:g.message(code:'default.label')}
                                                </td>
                                                <td class="middle">
                                                    ${entry?.inventoryItem?.lotNumber}
                                                </td>
                                                <td class="middle">
                                                    <g:formatDate date="${entry?.inventoryItem?.expirationDate}" format="MMM/yyyy"/>
                                                </td>
                                                <td class="middle">
                                                    ${entry?.quantity}
                                                </td>
                                                <td class="middle">
                                                    <g:if test="${selected}">
                                                        ${shipmentItemInstance?.quantity}
                                                    </g:if>
                                                </td>
                                            </tr>
                                        </g:each>
                                    </tbody>
                                </table>
                            </g:if>
                            <g:else>
                                <g:message code="default.empty.label"/>
                            </g:else>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td valign="top" class="name"></td>
                        <td valign="top" class="value">
                            <div class="left">
                                <button name="_eventId_pickShipmentItem" class="button">
                                    <warehouse:message code="default.button.save.label"/>
                                </button>
                                <button class="btnCloseDialog button">${g.message(code:'default.button.close.label')}</button>
                            </div>
                            <%--
                            <div class="right">
                                <span class="success">
                                    <g:message code="binLocation.recommended.label" default="Recommended bin location(s)"/>
                                </span>
                            </div>
                            --%>

                        </td>
                    </tr>
                    </tbody>
                </table>

            </div>

        </fieldset>
    </g:form>
</div>
<script>
    $(function() {
        $(".btnCloseDialog").click(function (event) {
            event.preventDefault();
            $('#dlgPickItem').dialog("close");
        });
    });
</script>

</body>
</html>