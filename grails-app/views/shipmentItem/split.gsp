<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title><g:message code="shipping.splitShipmentItem.label"/></title>
</head>

<body>

<div class="message">
    NOTE: At this time, the Split Item operation cannot be undone so please use caution before proceeding.
</div>

<div class="dialog">
    <g:form controller="createShipmentWorkflow" action="createShipment" params="[execution:params.execution]">
        <g:hiddenField name="id" value="${shipmentItemInstance?.shipment?.id}" />
        <g:hiddenField name="shipmentItem.id" value="${shipmentItemInstance?.id}" />
        <g:hiddenField name="version" value="${shipmentItemInstance?.version}" />

        <div class="dialog box">
            <h2>Original Item</h2>
            <table>
                <tbody>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="quantity"><warehouse:message code="shipmentItem.product.label" /></label>
                    </td>
                    <td valign="top" class="${hasErrors(bean: shipmentItemInstance, field: 'product', 'errors')}">
                        ${shipmentItemInstance?.inventoryItem?.product?.productCode}
                        <format:product product="${shipmentItemInstance?.inventoryItem?.product}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="binLocation"><warehouse:message code="shipmentItem.binLocation.label" /></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'binLocation', 'errors')}">
                        ${shipmentItemInstance?.binLocation?.name?:g.message(code:'default.label')}
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="lotNumber"><warehouse:message code="inventoryItem.lotNumber.label" /></label>
                    </td>
                    <td valign="top" class="${hasErrors(bean: shipmentItemInstance, field: 'lotNumber', 'errors')}">
                        ${shipmentItemInstance?.inventoryItem?.lotNumber}
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="expirationDate"><warehouse:message code="inventoryItem.expirationDate.label" /></label>
                    </td>
                    <td valign="top" class="${hasErrors(bean: shipmentItemInstance, field: 'inventoryItem', 'errors')}">
                        <g:expirationDate date="${shipmentItemInstance?.inventoryItem?.expirationDate}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="quantity"><warehouse:message code="shipmentItem.originalQuantity.label" default="Original Quantity"/></label>
                    </td>
                    <td valign="top" class="${hasErrors(bean: shipmentItemInstance, field: 'quantity', 'errors')}">
                        ${shipmentItemInstance?.quantity}
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <div id="errorMessage"></div>
        <table>
            <tr>
                <td style="margin:0;padding:0">
                    <div class="dialog box">
                        <h2>Shipment Item #1</h2>

                        <table>
                            <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="binLocation"><warehouse:message code="shipmentItem.binLocation.label" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'binLocation', 'errors')}" style="padding:0;margin:0">
                                    <g:if test="${binLocations}">
                                        <table>
                                            <thead>
                                            <tr>
                                                <th></th>
                                                <th><g:message code="default.bin.label" default="Bin"/></th>
                                                <th><g:message code="default.lot.label" default="Lot"/></th>
                                                <th><g:message code="default.exp.label" default="Exp"/></th>
                                                <th><g:message code="default.qty.label" default="Qty"/></th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <g:each var="entry" in="${binLocations}" status="status">
                                                <g:set var="statusClass" value="${entry.quantity>=shipmentItemInstance?.quantity?'':'error'}"/>
                                                <g:set var="selected" value="${entry?.binLocation?.id == shipmentItemInstance?.binLocation?.id &&
                                                        entry?.inventoryItem?.id == shipmentItemInstance?.inventoryItem?.id}"/>
                                                <tr class="${statusClass}">
                                                    <td class="middle">
                                                        <g:radio name="originalBinLocationAndInventoryItem" checked="${selected}" disabled="${!selected}"
                                                                 value="${entry?.binLocation?.id}:${entry?.inventoryItem?.id}" />
                                                    </td>
                                                    <td class="middle">
                                                        ${entry?.binLocation?.name?:g.message(code:'default.label')}
                                                    </td>
                                                    <td class="middle">
                                                        ${entry?.inventoryItem?.lotNumber}
                                                    </td>
                                                    <td class="middle">
                                                        <g:expirationDate date="${entry?.inventoryItem?.expirationDate}" />
                                                    </td>
                                                    <td class="middle">
                                                        ${entry?.quantity}
                                                    </td>
                                                </tr>
                                            </g:each>
                                            </tbody>
                                        </table>
                                    </g:if>
                                    <g:else>
                                        <g:message code="inventory.stockOut.message" default="Stock Out"/>
                                    </g:else>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="quantity"><warehouse:message code="shipmentItem.splitQuantity.label" default="Split Quantity" /></label>
                                </td>
                                <td valign="top" class="${hasErrors(bean: shipmentItemInstance, field: 'quantity', 'errors')}">
                                    <g:hiddenField id="originalQuantity" name="originalQuantity" value="${shipmentItemInstance?.quantity}"/>
                                    <g:textField id="oldQuantity" name="oldQuantity" value="${shipmentItemInstance?.quantity}" class="text" disabled="disabled"/>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </td>
                <td style="padding:0;margin:0">
                    <div class="dialog box">
                        <h2>Shipment Item #2</h2>
                        <table>
                            <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="binLocation"><warehouse:message code="shipmentItem.binLocation.label" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'binLocation', 'errors')}" style="padding:0;margin:0">
                                    <g:if test="${binLocations}">
                                        <table>
                                            <thead>
                                            <tr>
                                                <th></th>
                                                <th><g:message code="default.bin.label" default="Bin"/></th>
                                                <th><g:message code="default.lot.label" default="Lot"/></th>
                                                <th><g:message code="default.exp.label" default="Exp"/></th>
                                                <th><g:message code="default.qty.label" default="Qty"/></th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <g:each var="entry" in="${binLocations}" status="status">
                                                <g:set var="statusClass" value="${entry.quantity>=shipmentItemInstance?.quantity?'':'error'}"/>
                                                <g:set var="selected" value="${entry?.binLocation?.id == shipmentItemInstance?.binLocation?.id &&
                                                        entry?.inventoryItem?.id == shipmentItemInstance?.inventoryItem?.id}"/>
                                                <tr class="${statusClass}">
                                                    <td class="middle">
                                                        <g:radio name="selection" value="${entry?.binLocation?.id}:${entry?.inventoryItem?.id}" checked="${selected}"/>
                                                    </td>
                                                    <td class="middle">
                                                        ${entry?.binLocation?.name?:g.message(code:'default.label')}
                                                    </td>
                                                    <td class="middle">
                                                        ${entry?.inventoryItem?.lotNumber}
                                                    </td>
                                                    <td class="middle">
                                                        <g:expirationDate date="${entry?.inventoryItem?.expirationDate}" />
                                                    </td>
                                                    <td class="middle">
                                                        ${entry?.quantity}
                                                    </td>
                                                </tr>
                                            </g:each>
                                            </tbody>
                                        </table>
                                    </g:if>
                                    <g:else>
                                        <g:message code="inventory.stockOut.message" default="Stock Out"/>
                                    </g:else>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="quantity"><warehouse:message code="shipmentItem.splitQuantity.label" default="Split Quantity" /></label>
                                </td>
                                <td valign="top" class="${hasErrors(bean: shipmentItemInstance, field: 'quantity', 'errors')}">
                                    <g:textField id="splitQuantity" name="splitQuantity" value="" class="text" />

                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </td>
            </tr>
        </table>

        <hr/>
        <div class="buttons center" style="background-color: #eee;">
            <button name="_eventId_splitShipmentItem" class="button">
                <warehouse:message code="default.button.save.label"/>
            </button>
            <button class="btnCloseDialog button">${g.message(code:'default.button.close.label')}</button>
        </div>

    </g:form>
</div>
<script>

    $(function() {

        $("#splitQuantity").focus();
        $(".btnCloseDialog").click(function (event) {
            event.preventDefault();
            $('#dlgPickItem').dialog("close");
        });

        $("#splitQuantity").change(function(event) {
            $("#oldQuantity").effect("highlight", {}, 3000);
            $("#splitQuantity").effect("highlight", {}, 3000);
        })

        $("#splitQuantity").keyup(function(event){
            console.log(event);
            $("#errorMessage").html("");
            var originalQuantity = $("#originalQuantity").val();
            var oldQuantity = $("#oldQuantity").val();
            var splitQuantity = $("#splitQuantity").val();

            console.log("oldQuantity", oldQuantity);
            console.log("splitQuantity", splitQuantity);


            var newQuantity = originalQuantity - splitQuantity;
            if (splitQuantity > 0 && newQuantity > 0) {
                $("#oldQuantity").val(newQuantity);
            }
            else {
                $("#errorMessage").html("You must split the original quantity between bin locations so that the new quantities add up to the original quantity (" + oldQuantity + " + " + splitQuantity + " != " + originalQuantity +")");
                $("#splitQuantity").val('');
                $("#oldQuantity").val(originalQuantity);
                $(this).focus();
            }
        });
    });
</script>

</body>
</html>
