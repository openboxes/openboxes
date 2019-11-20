<g:if test="${requisitionItem.isCanceled() || requisitionItem?.isSubstituted() || requisitionItem?.isChanged()}">
    <div class="box" style="max-width: 600px;">
        <span class="fade">
            <format:metadata obj="${requisitionItem.status}"/>: ${warehouse.message(code: 'requisitionItem.cannotPickItems.label', default: 'Cannot pick stock for this requisition item.')}
        </span>
    </div>
</g:if>
<g:else>
    <g:form controller="requisition" action="addToPicklistItems">
        <g:hiddenField name="requisition.id" value="${requisitionItem?.requisition?.id}"/>
        <g:hiddenField name="requisitionItem.id" value="${requisitionItem?.id}"/>
        <div class="box">
            <table>
                <tr class="prop">
                    <td class="name">
                        <label><warehouse:message code="product.label"/>:</label>
                    </td>
                    <td class="value">
                        ${requisitionItem?.product?.productCode}
                        ${requisitionItem?.product?.name}
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><warehouse:message code="inventoryItem.quantity.label"/>:</label>
                    </td>
                    <td class="value">
                        <g:if test="${requisitionItem?.productPackage}">
                            ${requisitionItem?.quantity} ${requisitionItem?.productPackage?.uom?.code}/${requisitionItem?.productPackage?.quantity}
                        </g:if>
                        <g:else>
                            ${requisitionItem?.quantity} EA
                        </g:else>
                    </td>
                </tr>
            </table>
            <div class="availableInventoryItems" style="max-height: 350px; overflow: auto">
                <table>
                    <thead>
                    <tr class="prop">
                        <th colspan="4" class="center no-border-bottom border-right">
                            <h3>${warehouse.message(code: 'inventory.availableItems.label', default: 'Available items')}</h3>
                        </th>
                        <th colspan="3" class="center no-border-bottom">
                            <h3>${warehouse.message(code: 'picklist.picklistItems.label')}</h3>
                        </th>
                    </tr>
                    <tr class="prop">
                        <th>
                            ${warehouse.message(code: 'location.binLocation.label')}
                        </th>
                        <th>
                            ${warehouse.message(code: 'inventoryItem.lotNumber.label')}
                        </th>
                        <th>
                            ${warehouse.message(code: 'inventoryItem.expirationDate.label')}
                        </th>
                        <th class="center border-right">
                            ${warehouse.message(code: 'requisitionItem.quantityAvailable.label')}
                        </th>
                        <th class="center">
                            ${warehouse.message(code: 'picklistItem.quantity.label')}
                        </th>
                        <th class="center">
                            ${warehouse.message(code: 'product.uom.label')}
                        </th>
                        <th>
                            ${warehouse.message(code: 'default.actions.label')}
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                        <g:unless test="${availableItems}">
                            <tr style="height: 60px;">
                                <td colspan="7" class="center middle">
                                    <span class="fade">${warehouse.message(code: 'requisitionItem.noInventoryItems.label', default: 'No available items')}</span>
                                </td>
                            </tr>
                        </g:unless>
                        <g:each var="availableItem" in="${availableItems}" status="status">
                            <g:set var="binLocation" value="${availableItem.binLocation}"/>
                            <g:set var="inventoryItem" value="${availableItem.inventoryItem}"/>
                            <g:set var="picklistItems" value="${requisitionItem?.retrievePicklistItems()}"/>
                            <g:set var="picklistItem" value="${picklistItems?.find { it.binLocation == binLocation && it.inventoryItem == inventoryItem }}"/>
                            <g:set var="quantityPicked" value="${picklistItem?.quantity ?: 0}"/>
                            <g:set var="quantityRemaining" value="${requisitionItem?.calculateQuantityRemaining()?: 0}"/>
                            <tr class="prop ${status % 2 ? 'odd' : 'even'}">
                                <td class="middle">
                                    ${availableItem?.binLocation?.name?:warehouse.message(code:'default.label')}
                                </td>
                                <td class="middle">
                                    <span class="lotNumber">${inventoryItem?.lotNumber?:warehouse.message(code:'default.label')}</span>
                                </td>
                                <td class="middle">
                                    <g:if test="${inventoryItem?.expirationDate}">
                                        <g:formatDate date="${inventoryItem?.expirationDate}"
                                                format="d MMM yyyy"/>
                                    </g:if>
                                    <g:else>
                                        <span class="fade"><warehouse:message code="default.never.label"/></span>
                                    </g:else>
                                </td>
                                <td class="middle center border-right">
                                    <g:formatNumber number="${availableItem?.quantityAvailable ?: 0}" maxFractionDigits="0"/>
                                    ${inventoryItem?.product?.unitOfMeasure?:"EA"}
                                </td>
                                <td class="middle center">
                                    <g:hiddenField name="picklistItems[${status}].id" value="${picklistItem?.id}"/>
                                    <g:hiddenField name="picklistItems[${status}].requisitionItem.id" value="${requisitionItem?.id}"/>
                                    <g:hiddenField name="picklistItems[${status}].inventoryItem.id" value="${inventoryItem?.id}"/>
                                    <g:hiddenField name="picklistItems[${status}].binLocation.id" value="${binLocation?.id}"/>
                                    <input id="quantity-${requisitionItem?.id}-${status}"
                                           name="picklistItems[${status}].quantity"
                                            size="5"
                                           value="${quantityPicked}"
                                           class="quantity text"/>
                                </td>
                                <td class="middle center">
                                    ${inventoryItem?.product?.unitOfMeasure ?: "EA"}
                                </td>
                                <td>
                                    <div class="button-group">
                                        <button class="pick-action button" data-action="add" data-id="#quantity-${requisitionItem?.id}-${status}">+</button>
                                        <button class="pick-action button" data-action="subtract" data-id="#quantity-${requisitionItem?.id}-${status}">-</button>
                                        <button class="pick-action button" data-action="zero" data-id="#quantity-${requisitionItem?.id}-${status}">0</button>
                                        <button class="pick-action button" data-action="all" data-quantity="${requisitionItem?.quantity}" data-id="#quantity-${requisitionItem?.id}-${status}">*</button>
                                        <g:if test="${picklistItem}">
                                            <g:link controller="picklistItem"
                                                    action="delete"
                                                    id="${picklistItem?.id}"
                                                    class="button icon remove"
                                                    onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                                <warehouse:message code="picklist.removeFromPicklistItems.label" default="Remove"/>
                                            </g:link>
                                        </g:if>
                                    </div>

                                </td>
                            </tr>
                        </g:each>
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colspan="7">
                                <g:if test="${availableItems}">
                                    <div class="buttons">
                                        <g:if test="${requisitionItem?.retrievePicklistItems()}">
                                            <button class="button icon approve">
                                                ${warehouse.message(code: 'picklist.updatePicklistItems.label', default:'Update picklist')}
                                            </button>
                                        </g:if>
                                        <g:else>
                                            <button class="button icon add">
                                                ${warehouse.message(code: 'picklist.addToPicklistItems.label', default:'Add to picklist')}
                                            </button>
                                        </g:else>
                                    </div>
                                </g:if>
                            </td>
                        </tr>
                    </tfoot>
                </table>
            </div>
        </div>
    </g:form>
</g:else>
<script type="text/javascript">
	$(document).ready(function() {

        $(".quantity-picked input").keyup(function(){
           this.value=this.value.replace(/[^\d]/,'');
           $(this).trigger("change");//Safari and IE do not fire change event for us!
        });


        $(".pick-action").click(function(event){
            event.preventDefault();
            var id = $(this).data("id");
            var action = $(this).data("action");
            var quantityRequested = $(this).data("quantity");
            var quantityPicked = $(id);
            var quantityRemaining = quantityRequested - quantityPicked.val();

            switch (action) {

                case "add":
                    quantityPicked.val(+quantityPicked.val() + 1);
                    break;

                case "subtract":
                    if (+quantityPicked.val()>0) {
                        quantityPicked.val(+quantityPicked.val() - 1);
                    }
                    break;

                case "all":
                    if (quantityRemaining > 0) {
                        $(".quantity").val(0);
                        quantityPicked.val(quantityRemaining);
                    }
                    break;

                case "zero":
                    quantityPicked.val(0);
                    break;

                default:
                    break;
            }
            console.log($(this).data("id"));
        });
    });
</script>
