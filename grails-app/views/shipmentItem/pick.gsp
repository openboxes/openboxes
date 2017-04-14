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
                            ${shipmentItemInstance?.inventoryItem?.lotNumber}
                        </td>
                    </tr>
                    <tr class="prop">
                        <td valign="top" class="name">
                            <label for="expirationDate"><warehouse:message code="inventoryItem.expirationDate.label" /></label>
                        </td>
                        <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'inventoryItem', 'errors')}">
                            <g:formatDate date="${shipmentItemInstance?.inventoryItem?.expirationDate}"/>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td valign="top" class="name">
                            <label for="binLocation"><warehouse:message code="shipmentItem.binLocation.label" /></label>
                        </td>
                        <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'binLocation', 'errors')}">
                            <g:select name="binLocation.id" class="chzn-select-deselect"
                                      from="${binLocations}"
                                      noSelection="['null':'']"
                                      optionKey="id"
                                      optionValue="${{it.value}}"
                                      value="${shipmentItemInstance?.binLocation?.id}" />
                        </td>
                    </tr>


                    <tr class="prop">
                        <td valign="top" class="name">
                            <label for="quantity"><warehouse:message code="shipmentItem.quantity.label" /></label>
                        </td>
                        <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'quantity', 'errors')}">
                            <g:textField name="quantity" value="${shipmentItemInstance?.quantity}" class="text"/>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td valign="top" class="name"></td>
                        <td valign="top" class="value">
                            <div class="left">
                                <button name="_eventId_pickShipmentItem" class="button">
                                    <warehouse:message code="default.button.save.label"/>
                                </button>
                            </div>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </fieldset>
    </g:form>
</div>


</body>
</html>