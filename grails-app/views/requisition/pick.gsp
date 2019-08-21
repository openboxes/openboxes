<%@ page import="org.pih.warehouse.requisition.Requisition"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<script src="${createLinkTo(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript"></script>
<script src="${createLinkTo(dir:'js/', file:'requisition.js')}" type="text/javascript"></script>
<title><warehouse:message code="requisition.process.label" /></title>
<style>
	tr.selected-top { border-top: 5px solid #f3961c; border-right: 5px solid #f3961c; border-left: 5px solid #f3961c; }
    tr.selected, tr.selected a:not(.button) { color: black; font-size: 1.2em; text-decoration: blink; font-weight: bold; }
    tr.selected-middle { border-left: 5px solid #f3961c; }
    tr.selected-bottom { border-bottom: 5px solid #f3961c; border-right: 5px solid #f3961c; border-left: 5px solid #f3961c; }
</style>

</head>
<body>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		
		<g:render template="summary" model="[requisition:requisition]"/>

		<div class="yui-gf">
			<div class="yui-u first">
                <g:render template="header" model="[requisition:requisition]"/>


            </div>
            <div class="yui-u">


                <div id="picklist" class="left ui-validation">
                    <div class="">
                            <div class="box">
                                <h2><warehouse:message code="requisition.pick.label"/></h2>

                                <g:if test="${requisition.picklist}">
                                    <g:form controller="requisition" action="saveDetails">
                                        <g:hiddenField name="redirectAction" value="pick"/>
                                        <g:hiddenField name="id" value="${requisition?.id}"/>
                                        <table>
                                            <tr>
                                                <td class="middle center">
                                                    <label>
                                                        ${warehouse.message(code:'requisition.pickedBy.label', default: 'Picked by')}
                                                    </label>
                                                    <g:selectPerson id="pickedBy" name="picklist.picker" value="${requisition?.picklist?.picker?.id}"
                                                                    noSelection="['null':'']" size="40" class="chzn-select-deselect"/>
                                                </td>
                                                <td class="middle center">
                                                    <label>
                                                        ${warehouse.message(code:'requisition.datePicked.label', default: 'Date picked')}
                                                    </label>
                                                    <g:datePicker name="picklist.datePicked" value="${requisition?.picklist?.datePicked}"/>
                                                </td>
                                                <td class="middle center">
                                                    <button class="button icon approve">
                                                        ${warehouse.message(code:'default.button.save.label')}
                                                    </button>
                                                </td>
                                            </tr>

                                        </table>
                                    </g:form>
                                    <hr/>
                                </g:if>



                                <table>
                                    <thead>
                                        <tr class="odd">
                                            <th>

                                            </th>
                                            <th>
                                                ${warehouse.message(code: 'default.actions.label')}
                                            </th>
                                            <th class="center">
                                                <warehouse:message code="requisitionItem.status.label" default="Status" />
                                            </th>
                                            <th>
                                                ${warehouse.message(code: 'product.productCode.label')}
                                            </th>
                                            <th>
                                                ${warehouse.message(code: 'product.label')}
                                            </th>
                                            <th>
                                                ${warehouse.message(code: 'product.unitOfMeasure.label')}
                                            </th>
                                            <th class="center">
                                                ${warehouse.message(code: 'requisitionItem.quantityRequested.label')}
                                            </th>
                                            <th class="center">
                                                ${warehouse.message(code: 'requisitionItem.quantityPicked.label')}
                                            </th>
                                            <th class="center">
                                                ${warehouse.message(code: 'requisitionItem.quantityCanceled.label')}
                                            </th>
                                            <th class="center">
                                                ${warehouse.message(code: 'requisitionItem.quantityRemaining.label')}
                                            </th>
                                            <th class="center">
                                                <warehouse:message code="requisitionItem.orderIndex.label" default="Sort order" />
                                            </th>

                                        </tr>
                                    </thead>

                                    <tbody>
                                        <g:set var="status" value="${0}"/>
                                        <g:each var="requisitionItem" in="${requisition?.requisitionItems}" status="i">
                                            <g:if test="${!requisitionItem.parentRequisitionItem || true}">

                                                <g:set var="selected" value="${requisitionItem == selectedRequisitionItem }"/>
                                                <g:set var="noneSelected" value="${!selectedRequisitionItem }"/>
                                                <tr class="prop ${i%2?'odd':'even'} ${(requisitionItem?.isCanceled())?'canceled':''} ${selected ?'selected-middle':'unselected'}">

                                                <td class="middle">
                                                    <div class="action-menu">
                                                        <span class="action-btn">
                                                            <g:if test="${requisitionItem?.parentRequisitionItem}">
                                                                <img src="${resource(dir:'images/icons', file: 'indent.gif')}"/>

                                                            </g:if>
                                                            <g:else>
                                                                <g:if test="${requisitionItem?.isCanceled()}">
                                                                    <img src="${resource(dir:'images/icons/silk', file: 'decline.png')}"/>
                                                                </g:if>
                                                                <g:elseif test="${requisitionItem?.isSubstituted()}">
                                                                    <img src="${resource(dir:'images/icons/silk', file: 'arrow_switch.png')}"/>
                                                                </g:elseif>
                                                                <g:elseif test="${requisitionItem?.isApproved()}">
                                                                    <img src="${resource(dir:'images/icons/silk', file: 'accept.png')}"/>
                                                                </g:elseif>
                                                                <g:elseif test="${requisitionItem?.isChanged()}">
                                                                    <img src="${resource(dir:'images/icons/silk', file: 'pencil.png')}"/>
                                                                </g:elseif>
                                                                <g:elseif test="${requisitionItem?.isPending()}">
                                                                    <img src="${resource(dir:'images/icons/silk', file: 'hourglass.png')}"/>
                                                                </g:elseif>
                                                                <g:else>
                                                                    <img src="${resource(dir:'images/icons/silk', file: 'information.png')}"/>
                                                                </g:else>
                                                            </g:else>
                                                        </span>
                                                        <div class="actions">
                                                            <div class="box" style="width:450px;">
                                                                <div>
                                                                    C = ${requisitionItem?.id}
                                                                </div>
                                                                <div>
                                                                    P = ${requisitionItem?.parentRequisitionItem?.id}
                                                                </div>
                                                                <div>
                                                                    S = ${requisitionItem?.substitutionItem?.id}
                                                                </div>
                                                                <div>
                                                                    M = ${requisitionItem?.modificationItem?.id}
                                                                </div>

                                                                <div>
                                                                    <label>Requisition item:</label>
                                                                    ${requisitionItem.toJson()}
                                                                    ${requisitionItem.calculatePercentageCompleted()}
                                                                </div>
                                                                <div></div>
                                                                <div>
                                                                    <label>Substitution:</label>
                                                                    ${requisitionItem?.substitutionItem?.toJson()}
                                                                </div>
                                                                <div>
                                                                    <label>Modification:</label>
                                                                    ${requisitionItem?.modificationItem?.toJson()}
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>

                                                </td>

                                                <td class="left middle">
                                                    <a name="${selectedRequisitionItem?.id}"></a>
                                                    <g:if test="${!isChild }">
                                                        <div class="action-menu">
                                                            <button name="actionButtonDropDown" class="action-btn" id="requisitionItem-${requisitionItem?.id }-action">
                                                                <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
                                                            </button>
                                                            <div class="actions">
                                                                <g:if test="${requisitionItem.isCanceled() || requisitionItem?.isSubstituted() || requisitionItem?.isChanged()}">
                                                                    <div class="box" style="max-width: 600px;">

                                                                        <span class="fade">
                                                                            <format:metadata obj="${requisitionItem.status}"/>: ${warehouse.message(code: 'requisitionItem.cannotPickItems.label', default: 'Cannot pick stock for this requisition item.')}
                                                                        </span>
                                                                    </div>
                                                                </g:if>
                                                                <g:else>
                                                                    <div class="box" style="max-width: 800px;">
                                                                        <g:set var="inventoryItemMap" value="${requisitionItem?.retrievePicklistItems()?.groupBy { it?.inventoryItem }}"/>
                                                                        <g:form controller="requisition" action="addToPicklistItems">
                                                                            <g:hiddenField name="requisition.id" value="${requisition?.id}"/>
                                                                            <g:hiddenField name="requisitionItem.id" value="${requisitionItem?.id}"/>
                                                                            <h2>
                                                                                ${warehouse.message(code:'picklist.pickingItemsFor.label', default:'Picklist')}
                                                                                &rsaquo;
                                                                                ${requisitionItem?.product?.productCode}
                                                                                ${requisitionItem?.product?.name}
                                                                            </h2>
                                                                            <div>
                                                                                <table>
                                                                                    <tr class="prop">
                                                                                    <td>
                                                                                        <label><warehouse:message code="product.label"/>:</label>
                                                                                    </td>
                                                                                    <td>
                                                                                        ${requisitionItem?.product?.productCode}
                                                                                        ${requisitionItem?.product?.name}

                                                                                    </td>
                                                                                </tr>
                                                                                <tr class="prop">
                                                                                    <td>
                                                                                        <label><warehouse:message code="inventoryItem.quantity.label"/>:</label>
                                                                                    </td>
                                                                                    <td>
                                                                                        <g:if test="${requisitionItem?.productPackage}">
                                                                                            ${requisitionItem?.quantity} ${requisitionItem?.productPackage?.uom?.code}/${requisitionItem?.productPackage?.quantity}
                                                                                        </g:if>
                                                                                        <g:else>
                                                                                            ${requisitionItem?.quantity} EA
                                                                                        </g:else>

                                                                                    </td>
                                                                                </tr>
                                                                                <tr class="prop">
                                                                                    <td>
                                                                                        <label><warehouse:message code="inventoryLevel.binLocation.label"/>:</label>
                                                                                    </td>
                                                                                    <td>
                                                                                        ${picklistItem?.inventoryItem?.product?.getInventoryLevel(session?.warehouse?.id)?.binLocation?:warehouse.message(code:'default.none.label')}

                                                                                    </td>
                                                                                </tr>
                                                                            </table>

                                                                            <div class="availableInventoryItems" style="max-height: 350px; overflow: auto">

                                                                                <table>

                                                                                    <thead>
                                                                                    <tr class="prop">
                                                                                        <th colspan="3" class="center no-border-bottom border-right">
                                                                                            ${warehouse.message(code: 'inventory.availableItems.label', default: 'Available items')}
                                                                                        </th>
                                                                                        <th colspan="4" class="center no-border-bottom">
                                                                                            ${warehouse.message(code: 'picklist.picklistItems.label')}
                                                                                        </th>
                                                                                    </tr>
                                                                                    <tr>
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
                                                                                    <g:set var="inventoryItems" value="${productInventoryItemsMap[requisitionItem?.product?.id]?.findAll { it.quantity > 0 }}"/>
                                                                                    <g:unless test="${inventoryItems}">
                                                                                        <tr style="height: 60px;">
                                                                                            <td colspan="7" class="center middle">
                                                                                                <span class="fade">${warehouse.message(code: 'requisitionItem.noInventoryItems.label', default: 'No available items')}</span>
                                                                                            </td>
                                                                                        </tr>
                                                                                    </g:unless>
                                                                                    <g:each var="inventoryItem" in="${inventoryItems}" status="status">
                                                                                        <g:set var="picklistItem" value="${inventoryItemMap[inventoryItem]?.first()}"/>
                                                                                        <g:set var="quantityPicked" value="${inventoryItemMap[inventoryItem]?.first()?.quantity ?: 0}"/>
                                                                                        <g:set var="quantityRemaining" value="${requisitionItem?.calculateQuantityRemaining()?: 0}"/>
                                                                                        <tr class="prop ${status % 2 ? 'odd' : 'even'}">
                                                                                            <td class="middle">
                                                                                                <span class="lotNumber">${inventoryItem?.lotNumber?:warehouse.message(code:'default.none.label')}</span>
                                                                                            </td>
                                                                                            <td class="middle">
                                                                                                <g:if test="${inventoryItem?.expirationDate}">
                                                                                                    <g:formatDate
                                                                                                            date="${inventoryItem?.expirationDate}"
                                                                                                            format="d MMM yyyy"/>
                                                                                                </g:if>
                                                                                                <g:else>
                                                                                                    <span class="fade"><warehouse:message code="default.never.label"/></span>
                                                                                                </g:else>
                                                                                            </td>
                                                                                            <td class="middle center border-right">
                                                                                                ${inventoryItem?.quantity ?: 0}
                                                                                                ${inventoryItem?.product?.unitOfMeasure?:"EA"}
                                                                                            </td>
                                                                                            <td class="middle center">
                                                                                                <g:hiddenField name="picklistItems[${status}].id" value="${picklistItem?.id}"/>
                                                                                                <g:hiddenField name="picklistItems[${status}].requisitionItem.id" value="${picklistItem?.requisitionItem?.id?:requisitionItem?.id}"/>
                                                                                                <g:hiddenField name="picklistItems[${status}].inventoryItem.id" value="${picklistItem?.inventoryItem?.id?:inventoryItem?.id}"/>
                                                                                                <input id="quantity-${requisitionItem?.id}-${status}" name="picklistItems[${status}].quantity" value="${quantityPicked}" size="5" type="text" class="quantity text"/>
                                                                                            </td>
                                                                                            <td class="middle center">
                                                                                                ${inventoryItem?.product?.unitOfMeasure ?: "EA"}
                                                                                            </td>
                                                                                            <td>
                                                                                                <g:if test="${picklistItem}">
                                                                                                    <g:link controller="picklistItem"
                                                                                                            action="delete"
                                                                                                            id="${picklistItem?.id}"
                                                                                                            class="button icon remove"
                                                                                                            onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                                                                                        <warehouse:message code="picklist.removeFromPicklistItems.label" default="Remove"/>
                                                                                                    </g:link>
                                                                                                </g:if>

                                                                                                <button class="pick-action" data-action="add" data-id="#quantity-${requisitionItem?.id}-${status}">+</button>
                                                                                                <button class="pick-action" data-action="subtract" data-id="#quantity-${requisitionItem?.id}-${status}">-</button>
                                                                                                <button class="pick-action" data-action="zero" data-id="#quantity-${requisitionItem?.id}-${status}">0</button>
                                                                                                <button class="pick-action" data-action="all" data-quantity="${requisitionItem?.quantity}" data-id="#quantity-${requisitionItem?.id}-${status}">*</button>

                                                                                            </td>
                                                                                        </tr>

                                                                                    </g:each>
                                                                                    </tbody>
                                                                                </table>
                                                                            </div>
                                                                            <g:if test="${inventoryItems}">
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
                                                                        </g:form>

                                                                    </div>
                                                                </g:else>
                                                            </div>
                                                        </div>
                                                    </g:if>
                                                </td>
                                                <td class="middle">
                                                    <div class="${requisitionItem?.isCanceled()?'canceled':''}" title="${requisitionItem?.cancelReasonCode}">
                                                        <format:metadata obj="${requisitionItem.status}"/>
                                                    </div>
                                                </td>
                                                <td class="middle">
                                                    ${requisitionItem?.product?.productCode}
                                                </td>
                                                <td class="middle">
                                                    <g:link controller="requisition" action="pick" id="${requisition.id }" params="['requisitionItem.id':requisitionItem?.id]">
                                                        <format:product product="${requisitionItem?.product}"/>
                                                    </g:link>
                                                </td>
                                                <td class="middle">
                                                    <g:if test="${requisitionItem?.productPackage}">
                                                        ${requisitionItem?.productPackage?.uom?.code}/${requisitionItem?.productPackage?.quantity}
                                                    </g:if>
                                                    <g:else>
                                                        ${requisitionItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }
                                                    </g:else>
                                                </td>
                                                <td class="center middle">
                                                    ${requisitionItem?.quantity?:0 }
                                                </td>
                                                <td class="center middle">
                                                    ${requisitionItem?.calculateQuantityPicked()?:0 }
                                                </td>
                                                <td class="center middle">
                                                    ${requisitionItem?.quantityCanceled?:0}
                                                </td>
                                                <td class="center middle">
                                                    <g:set var="quantityRemaining" value="${requisitionItem?.calculateQuantityRemaining()?:0 }"/>
                                                    <div class="${(quantityRemaining > 0)?'error':'info'}">
                                                        ${quantityRemaining }
                                                    </div>

                                                </td>
                                                <td class="center middle">
                                                    ${requisitionItem.orderIndex}
                                                </td>
                                            </tr>
                                            </g:if>
                                        </g:each>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    <div class="clear"></div>
                    <g:unless test="${params.edit}">
                        <div class="buttons center">
                            <g:link controller="requisition" action="review" id="${requisition.id }" class="button icon arrowleft">
                                <warehouse:message code="default.button.back.label"/>
                            </g:link>

                            <g:link controller="requisition" action="picked" id="${requisition.id }" class="button icon arrowright">
                                <warehouse:message code="default.button.next.label"/>
                            </g:link>
                        </div>
                    </g:unless>
                </div>
			</div>
		</div>
	    	
<script type="text/javascript">
	$(document).ready(function() {

    	$(".tabs").tabs(
   			{
   				cookie: {
   					// store cookie for a day, without, it would be a session cookie
   					expires: 1
   				}
   			}
		).addClass('ui-tabs-vertical ui-helper-clearfix');
        $(".tabs li").removeClass('ui-corner-top').addClass('ui-corner-left');

        $("#accordion").accordion({
          header: ".accordion-header", 
          icons: false, 
          active:false,
          collapsible: true,
          heightStyle: "content"
          });

        $("#cancelRequisition").click(function() {
            if(confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}')) {
                return true;
            }
        });

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

	$("#searchable-suggest").autocomplete({
		select: function(event, ui) {
			console.log(ui.item);
			$("#substitutionItemDetails").show();
			
			var expirationDate = new Date(ui.item.expirationDate)
			var expirationDateString = expirationDate.getMonth()+1 + "/" + expirationDate.getDate() + "/" + expirationDate.getFullYear()
			$("#productId").val(ui.item.productId);
			$("#productName").text(ui.item.productName);
			$("#inventoryItemId").val(ui.item.id);
			$("#lotNumber").val(ui.item.lotNumber);
			
			$("#lotNumberText").text(ui.item.lotNumber);
			$("#expirationDateText").text(expirationDateString);
			$("#maxQuantityText").text(ui.item.quantity);
			$("#quantity").focus();
			$(this).val('');
		    return false;
		}
	});

    });
</script>

</body>
</html>
