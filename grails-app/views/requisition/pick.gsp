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
    tr.selected-middle { border-right: 5px solid #f3961c; border-left: 5px solid #f3961c; }
    tr.selected-bottom { border-bottom: 5px solid #f3961c; border-right: 5px solid #f3961c; border-left: 5px solid #f3961c; }
    /*tr.unselected, tr.unselected a { color: #ccc; }*/
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

                            <%--
                            <g:link controller="requisitionItem" action="substitute" class="button">Substitute</g:link>
                            <g:link controller="requisitionItem" action="cancel" class="button">Cancel remaining</g:link>
                            --%>
                            <div class="box">
                                <h2><warehouse:message code="requisition.process.label"/></h2>
                                    <table border="0">
                                        <tr>
                                            <td class="left" width="1%">
                                                <div>
                                                    <g:link controller="requisition"
                                                            action="pickPreviousItem"
                                                            id="${requisition?.id}"
                                                            params="['requisitionItem.id': selectedRequisitionItem?.id]"
                                                            class="button icon arrowup"
                                                            style="width:100px;"
                                                            fragment="${selectedRequisitionItem?.previousRequisitionItem?.id}">${warehouse.message(code:'default.button.previous.label')}</g:link>
                                                </div>
                                                <div>
                                                    <g:link controller="requisition" action="pickNextItem"
                                                            id="${requisition?.id}"
                                                            params="['requisitionItem.id': selectedRequisitionItem?.id]"
                                                            class="button icon arrowdown"
                                                            style="width:100px;"
                                                            fragment="${selectedRequisitionItem?.nextRequisitionItem?.id}">${warehouse.message(code:'default.button.next.label')}</g:link>
                                                </div>
                                            </td>
                                            <td class="left">
                                                <g:each var="requisitionItem" in="${requisition.requisitionItems}" status="${status}">
                                                    <g:set var="isActive" value="${requisitionItem == selectedRequisitionItem}"/>
                                                    <g:link controller="requisition" action="pick" id="${requisition.id }"
                                                            params="['requisitionItem.id':requisitionItem?.id]" fragment="${requisitionItem.id}">
                                                        <span class="count ${isActive?'active':''}">${status+1}</span>
                                                    </g:link>
                                                </g:each>


                                            </td>

                                        </tr>
                                    </table>

                                <table>
                                    <g:if test="${!selectedRequisitionItem}">
                                        <thead>
                                            <tr class="odd">
                                                <th>

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
                                                <th>
                                                    ${warehouse.message(code: 'requisitionItem.status.label')}
                                                </th>
                                                <th>
                                                    %
                                                </th>
                                                <th>
                                                    ${warehouse.message(code: 'default.actions.label')}
                                                </th>
                                            </tr>
                                        </thead>
                                    </g:if>
                                    <tbody>
                                        <g:each var="${requisitionItem }" in="${requisition?.requisitionItems }" status="i">
                                            <g:render template="pickRequisitionItem"
                                                      model="[i:i,requisitionItem:requisitionItem,selectedRequisitionItem:selectedRequisitionItem]"/>
                                        </g:each>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="clear"></div>


                <div class="buttons center">
                    <g:link controller="requisition" action="review" id="${requisition.id }" class="button">
                        <warehouse:message code="default.button.back.label"/>
                    </g:link>

                    <g:link controller="requisition" action="picked" id="${requisition.id }" class="button">
                        <warehouse:message code="default.button.next.label"/>
                    </g:link>
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


        /*
        $(".dialog-box").dialog({width:600,height:400,position: {
            my: 'bottom',
            at: 'top',
            of: $('#selected-requisition-item')
        }});
    	*/

        //$("#requisitionForm").validate({ submitHandler: viewModel.save });

        $("#accordion").accordion({
          header: ".accordion-header", 
          icons: false, 
          active:false,
          collapsible: true,
          heightStyle: "content"
          });

        //setInterval(function () { saveToLocal(); }, 3000);

        $("#cancelRequisition").click(function() {
            if(confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}')) {
                //openboxes.requisition.deletePicklistFromLocal(picklistFromServer.id);
                return true;
            }
        });

        $(".quantity-picked input").keyup(function(){
           this.value=this.value.replace(/[^\d]/,'');      
           $(this).trigger("change");//Safari and IE do not fire change event for us!
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
			//$("#quantity").val(ui.item.quantity);
			$("#quantity").focus();
			$(this).val('');
		    return false;
		}
	});

    });
</script>

</body>
</html>
