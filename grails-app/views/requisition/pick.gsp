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
                                            <tr class="prop">
                                                <td class="name">
                                                    <label>
                                                        ${warehouse.message(code:'requisition.pickedBy.label', default: 'Picked by')}
                                                    </label>
                                                </td>
                                                <td class="value">
                                                    <g:selectPerson id="pickedBy" name="picklist.picker" value="${requisition?.picklist?.picker?.id}"
                                                                    noSelection="['null':'']" size="40" class="chzn-select-deselect"/>
                                                </td>
                                            </tr>
                                            <tr class="prop">
                                                <td class="name">
                                                    <label>
                                                        ${warehouse.message(code:'requisition.datePicked.label', default: 'Date picked')}
                                                    </label>
                                                </td>
                                                <td class="value">
                                                    <g:datePicker name="picklist.datePicked" value="${requisition?.picklist?.datePicked}"/>

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
                                                ${warehouse.message(code: 'default.actions.label')}
                                            </th>
                                            <th class="center">

                                            </th>
                                            <th>

                                            </th>
                                            <th class="center">
                                                <warehouse:message code="requisitionItem.status.label" default="Status" />
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
                                        </tr>
                                    </thead>

                                    <tbody>
                                        <g:set var="status" value="${0}"/>
                                        <g:each var="requisitionItem" in="${requisition?.requisitionItems}" status="i">
                                            <g:if test="${!requisitionItem.parentRequisitionItem || true}">
                                                <g:set var="selected" value="${requisitionItem == selectedRequisitionItem }"/>
                                                <g:set var="noneSelected" value="${!selectedRequisitionItem }"/>
                                                <tr class="prop ${i%2?'odd':'even'} ${(requisitionItem?.isCanceled())?'canceled':''} ${selected ?'selected-middle':'unselected'}">
                                                    <td class="left middle">
                                                        <a name="${selectedRequisitionItem?.id}"></a>
                                                        <g:if test="${!isChild }">

                                                            <a href="javascript:void(0);" class="button btn-show-dialog" data-title="${g.message(code:'default.edit.label', args: [g.message(code:'picklist.label')])}"
                                                               data-url="${request.contextPath}/requisition/showPicklistDialog?id=${requisitionItem?.id}">
                                                                <img src="${resource(dir: 'images/icons/silk', file: 'cart_add.png')}"/>&nbsp;
                                                                <g:message code="default.button.pick.label" default="Pick"/>
                                                            </a>

                                                        </g:if>
                                                    </td>
                                                    <td class="center middle">
                                                        ${requisitionItem.orderIndex+1}
                                                    </td>
                                                    <td class="middle">
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
                                                    </td>
                                                    <td class="middle">
                                                        <div class="${requisitionItem?.isCanceled()?'canceled':''}" title="${requisitionItem?.cancelReasonCode}">
                                                            <format:metadata obj="${requisitionItem.status}"/>
                                                        </div>
                                                    </td>
                                                    <td class="middle">
                                                        ${requisitionItem?.product?.productCode}
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
                                                </tr>
                                            </g:if>
                                        </g:each>
                                    </tbody>
                                    <tfoot>
                                        <tr>
                                            <td colspan="11">
                                                <div class="center buttons">
                                                    <g:link controller="requisition" action="review" id="${requisition.id }" class="button icon arrowleft">
                                                        <warehouse:message code="default.button.back.label"/>
                                                    </g:link>
                                                    <g:link controller="requisition" action="picked" id="${requisition.id }" class="button icon arrowright">
                                                        <warehouse:message code="default.button.next.label"/>
                                                    </g:link>
                                                </div>
                                            </td>
                                        </tr>
                                    </tfoot>
                                </table>
                            </div>
                        </div>
                    </div>
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
