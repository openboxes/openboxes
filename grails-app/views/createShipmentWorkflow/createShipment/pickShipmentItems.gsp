  
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title><warehouse:message code="shipping.pickShipmentItems.label"/></title>
         <style>
         	.top-border { border-top: 2px solid lightgrey; }
         	.right-border { border-right: 2px solid lightgrey; }
             .active { background-color: #b2d1ff;  }
             .active td { color: #666; }
             .same-lot-number td { font-weight: bold }
             .different-product { border-top: 3px solid lightgrey; }

         </style>
    </head>
    <body>
        <div class="body">
            ${flash.message}
        	<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
            <g:if test="${message}">
                <div class="message">${message}</div>
            </g:if>
            <g:hasErrors bean="${command}">
				<div class="errors">
					<g:renderErrors bean="${command}" as="list" />
				</div>
			</g:hasErrors>
			<g:hasErrors bean="${shipmentInstance}">
				<div class="errors">
					<g:renderErrors bean="${shipmentInstance}" as="list" />
				</div>
			</g:hasErrors>

            <g:render template="../shipment/summary" />
            <g:render template="flowHeader" model="['currentState':'Picking']"/>

            <g:set var="shipmentItemsSorted" value="${shipmentInstance.sortShipmentItems()}"/>

            <%-- If there's no selected item, we should select the first item from the list --%>
            <%--
            <g:if test="${!shipmentItemSelected}">
                <g:set var="shipmentItemSelected" value="${shipmentItemsSorted?.iterator().next()}"></g:set>
            </g:if>
            --%>

            <div class="yui-gc">
                <div class="yui-u first">
                    <g:form action="createShipment" method="post">
                        <g:hiddenField name="id" value="${shipmentInstance?.id}"/>
                        <g:hiddenField name="shipment.id" value="${shipmentInstance?.id}"/>

                        <div id="picklistItemsBox" class="dialog box">
                            <h2>
                                <img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}"/>
                                <warehouse:message code="shipping.pickShipmentItems.label"/>
                            </h2>

                            <g:if test="${session.warehouse == shipmentInstance?.origin}">

                                <table>
                                    <thead>

                                    <tr>
                                        <th class="right-border"><g:message code="container.label"/></th>
                                        <th><g:message code="product.label"/></th>
                                        <th><g:message code="inventoryItem.lotNumber.label"/></th>
                                        <th><g:message code="inventoryItem.expirationDate.label"/></th>
                                        <th><g:message code="location.binLocation.label"/></th>
                                        <th class="center"><g:message code="default.qty.label"/></th>
                                        <th class="center"><g:message code="default.uom.label"/></th>
                                    </tr>
                                    </thead>
                                    <g:set var="previousContainer"/>
                                    <g:set var="previousProduct"/>
                                    <tbody>
                                    <g:each var="shipmentItem" in="${shipmentItemsSorted}" status="status">
                                        <g:set var="binLocations" value="${quantityMap ? quantityMap[shipmentItem?.inventoryItem?.product] : []}"/>
                                        <g:set var="binLocationSelected" value="${binLocations.findAll{it.binLocation == shipmentItem.binLocation && it.inventoryItem==shipmentItem?.inventoryItem}}"/>


                                        <g:if test="${binLocations}">
                                            <g:set var="totalQtyByProduct" value="${binLocations.sum { it.quantity }}"/>
                                        </g:if>
                                        <g:set var="totalQtyByBin" value="${binLocationSelected.sum { it.quantity }}"/>
                                        <g:set var="availableInBin" value="${totalQtyByBin >= shipmentItem?.quantity}"/>
                                        <g:set var="availableInProduct" value="${totalQtyByProduct >= shipmentItem?.quantity}"/>

                                        <g:set var="isSameAsPreviousContainer" value="${shipmentItem?.container == previousContainer}"/>
                                        <g:set var="isSameAsPreviousProduct" value="${shipmentItem?.product == previousProduct}"/>
                                        <g:set var="isActive" value="${shipmentItem == shipmentItemSelected}"/>
                                        <g:set var="href" value="${g.createLink(action:'createShipment', event:'pickShipmentItem2', id:shipmentItem?.id)}"/>

                                        <tr data-href="${href}"
                                            class="clickable-row ${isActive?'active':''} ${status % 2 ? 'even' : 'odd' } ${!isSameAsPreviousContainer ? 'top-border':'' } ${!isSameAsPreviousProduct ? 'different-product':'' }">

                                            <td class="top right-border">
                                                <a name="shipmentItem-${shipmentItem?.id}"></a>
                                                <g:if test="${!isSameAsPreviousContainer }">
                                                    <g:if test="${shipmentItem?.container}">
                                                        ${shipmentItem?.container?.name }
                                                    </g:if>
                                                    <g:else>
                                                        <g:message code="default.label"/>
                                                    </g:else>
                                                </g:if>
                                            </td>
                                            <td class="top">
                                                <g:link controller="inventoryItem" action="showStockCard" params="['product.id':shipmentItem?.inventoryItem?.product?.id]">
                                                    ${shipmentItem?.inventoryItem?.product?.productCode}
                                                    <format:product product="${shipmentItem?.inventoryItem?.product}"/>
                                                </g:link>
                                            </td>
                                            <td class="top">
                                                <span class="lotNumber">${shipmentItem?.inventoryItem?.lotNumber }</span>
                                            </td>
                                            <td class="top">
                                                <g:if test="${shipmentItem?.inventoryItem?.expirationDate}">
                                                    <span class="expirationDate">
                                                        <g:formatDate date="${shipmentItem?.inventoryItem?.expirationDate }" format="d MMMMM yyyy"/>
                                                    </span>
                                                </g:if>
                                                <g:else>
                                                    <span class="fade">
                                                        ${warehouse.message(code: 'default.never.label')}
                                                    </span>
                                                </g:else>
                                            </td>
                                            <td class="top">
                                                <div class="binLocation">
                                                    <g:if test="${shipmentItem?.binLocation}">
                                                        ${shipmentItem?.binLocation?.name}
                                                    </g:if>
                                                    <g:else>
                                                        ${g.message(code: 'default.label')}
                                                    </g:else>
                                                </div>
                                            </td>
                                            <td class="top center">
                                                ${shipmentItem?.quantity }
                                            </td>
                                            <td class="top center">
                                                ${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }
                                            </td>

                                        </tr>
                                        <g:set var="previousContainer" value="${shipmentItem?.container }"/>
                                        <g:set var="previousProduct" value="${shipmentItem?.product }"/>
                                    </g:each>
                                    </tbody>
                                    <tfoot>

                                    <tr>
                                        <td>
                                        </td>
                                        <td colspan="11" class="left">
                                            <%--
                                                <g:submitButton name="autoPickShipmentItems" value="${g.message(code:'shipping.autoPickItems.label')}" class="button"></g:submitButton>
                                            --%>
                                        </td>
                                    </tr>
                                    </tfoot>

                                </table>
                            </g:if>
                            <g:else>
                                <div class="empty fade center">
                                    <g:message code="shipping.pickShipmentItemsForOutbound.message"/>
                                </div>
                            </g:else>

                        </div>
                        <div class="buttons">
                            <button name="_eventId_back" class="button">&lsaquo; <warehouse:message code="default.button.back.label"/></button>
                            <button name="_eventId_next" class="button"><warehouse:message code="default.button.next.label"/> &rsaquo;</button>
                            <button name="_eventId_save" class="button"><warehouse:message code="default.button.saveAndExit.label"/></button>
                            <button name="_eventId_cancel" class="button"><warehouse:message code="default.button.cancel.label"/></button>
                        </div>

                    </g:form>
                </div>
                <div class="yui-u">

                    <g:set var="binLocationSelected" value="${binLocationsSelected.findAll{it?.binLocation == shipmentItemSelected?.binLocation && it.inventoryItem==shipmentItemSelected?.inventoryItem}}"/>

                    <div class="box" id="pickShipmentItemBox" style="position: absolute; width: 30%">
                        <h2>
                            <img src="${createLinkTo(dir:'images/icons',file:'draggable.png')}"
                                 title="${g.message(code:'picklist.picked.label')}">
                            <g:message code="shipping.editPicklistItem.label" default="Edit Picklist Item"/></h2>

                        <div class="dialog">
                            <g:form controller="createShipmentWorkflow" action="createShipment" params="[execution:params.execution]" autocomplete="off">
                                <g:hiddenField name="id" value="${shipmentItemSelected?.shipment?.id}" />
                                <g:hiddenField name="shipmentItem.id" value="${shipmentItemSelected?.id}" />
                                <g:hiddenField name="version" value="${shipmentItemSelected?.version}" />
                                <g:hiddenField name="currentShipmentItemId" value="${shipmentItemSelected?.id}"/>

                                <g:if test="${!shipmentItemSelected}">
                                    <div class="empty fade center">
                                        <g:message code="shipping.pickShipmentItems.message" default="Select an item from the picklist"/>
                                        <g:link action="createShipment" event="nextShipmentItem" id="${shipmentItemSelected?.id}" class="button" fragment="shipmentItem-${shipmentItemSelected?.id}">
                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'next_blue.png')}" alt="Next Item"/>&nbsp;
                                            <g:message code="default.button.next.label" default="Next"/>
                                        </g:link>
                                    </div>


                                </g:if>
                                <g:else>
                                    <table>
                                        <tr class="prop">
                                            <td class="name">
                                                <label><g:message code="default.status.label"/></label>
                                            </td>
                                            <td class="value">

                                                <g:if test="${binLocationsSelected}">
                                                    <g:set var="totalQtyByProduct" value="${binLocationsSelected.sum { it.quantity }}"/>
                                                </g:if>
                                                <g:set var="totalQtyByBin" value="${binLocationSelected.sum { it.quantity }}"/>
                                                <g:set var="availableInBin" value="${totalQtyByBin >= shipmentItemSelected?.quantity}"/>
                                                <g:set var="availableInProduct" value="${totalQtyByProduct >= shipmentItemSelected?.quantity}"/>
                                                <g:if test="${availableInBin}">
                                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}"
                                                         title="${g.message(code:'picklist.picked.label')}">
                                                    ${g.message(code:'picklist.picked.message', default: 'Item has been picked')}
                                                </g:if>
                                                <g:elseif test="${availableInProduct}">
                                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'decline.png')}"
                                                         title="${g.message(code:'picklist.notPicked.label', default: 'Not Picked')}">
                                                    ${g.message(code:'picklist.notAvailable.message', default: 'Item has not been picked')}
                                                </g:elseif>
                                                <g:else>
                                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'decline.png')}"
                                                         title="${g.message(code:'picklist.notAvailable.label', default: 'Not Available')}">
                                                    ${g.message(code:'picklist.notAvailable.message', default: 'Insufficient quantity available for item')}
                                                </g:else>

                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td class="name">
                                                <label><g:message code="product.label"/></label>
                                            </td>
                                            <td class="value">
                                                ${shipmentItemSelected?.inventoryItem?.product?.name}
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td class="name">
                                                <label><g:message code="location.binLocation.label"/></label>
                                            </td>
                                            <td class="value">

                                                <g:if test="${binLocationsSelected}">

                                                    <table id="tableBinLocations">
                                                        <thead>
                                                        <tr>
                                                            <th></th>
                                                            <th><g:message code="product.productCode.label" default="Code"/></th>
                                                            <th><g:message code="default.bin.label" default="Bin"/></th>
                                                            <th><g:message code="default.lot.label" default="Lot"/></th>
                                                            <th><g:message code="default.exp.label" default="Exp"/></th>
                                                            <th><g:message code="default.qty.label" default="Qty"/></th>
                                                        </tr>
                                                        </thead>
                                                        <tbody>
                                                        <g:each var="entry" in="${binLocationsSelected}" status="status">
                                                            <g:set var="statusClass" value="${entry.quantity>=shipmentItemSelected?.quantity?'':''}"/>
                                                            <g:set var="selected" value="${entry?.binLocation?.id == shipmentItemSelected?.binLocation?.id &&
                                                                    entry?.inventoryItem?.id == shipmentItemSelected?.inventoryItem?.id}"/>
                                                            <g:set var="isSameLotNumber" value="${entry?.inventoryItem?.id==shipmentItemSelected?.inventoryItem?.id}"/>

                                                            <tr class="${selected?'active':''} ${statusClass} ${isSameLotNumber?'same-lot-number':''}">
                                                                <td class="middle">
                                                                    <g:radio name="binLocationAndInventoryItem" value="${entry?.binLocation?.id}:${entry?.inventoryItem?.id}"
                                                                             checked="${selected}"/>
                                                                </td>
                                                                <td>
                                                                    ${entry?.inventoryItem?.product?.productCode}
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
                                                            </tr>
                                                        </g:each>
                                                        </tbody>
                                                        <tfoot>
                                                            <tr class="prop">
                                                                <th colspan="5">
                                                                    <g:message code="default.total.label"/>
                                                                </th>
                                                                <th class="center">
                                                                    ${totalQtyByProduct?:0}
                                                                </th>
                                                            </tr>
                                                        </tfoot>
                                                    </table>
                                                </g:if>
                                                <g:else>
                                                    <div class="fade">
                                                        <g:message code="shipping.noBinLocation.label" default="There are no bin locations for item {0}"
                                                                   args="[shipmentItemSelected?.inventoryItem?.lotNumber]"/>

                                                    </div>
                                                </g:else>
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td class="name">
                                                <label><g:message code="default.quantity.label"/></label>
                                            </td>
                                            <td class="value">
                                                <g:textField id="quantity" name="quantity" value="${shipmentItemSelected?.quantity}" class="text"/>
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td class="name">

                                            </td>
                                            <td class="value">
                                                <button name="_eventId_pickShipmentItem" class="button">
                                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}" alt="Save Item"/>&nbsp;
                                                    <g:message code="default.button.save.label"/>
                                                </button>

                                                <g:link action="createShipment" event="splitShipmentItem2" id="${shipmentItemSelected?.id}" class="button">
                                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_divide.png')}" alt="Split Item"/>&nbsp;
                                                    <warehouse:message code="shipping.button.splitItem.label"/>
                                                </g:link>

                                                <g:link action="createShipment" event="deleteShipmentItem" id="${shipmentItemSelected?.id}"
                                                        onclick="return confirm('Are you sure you want to delete this item? NOTE: If this is a split item, quantity will not be returned to the original item.')" class="button">
                                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'decline.png')}" alt="Delete Item"/>&nbsp;
                                                    <g:message code="shipping.button.deleteItem.label"/>
                                                </g:link>

                                                <g:link action="createShipment" event="nextShipmentItem" id="${shipmentItemSelected?.id}" class="button">
                                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'next_blue.png')}" alt="Next Item"/>&nbsp;
                                                    <g:message code="default.button.next.label" default="Next"/>
                                                </g:link>
                                            </td>
                                        </tr>
                                    </table>
                                </g:else>

                            </g:form>
                        </div>
                    </div>
                </div>
            </div>
		</div>

        <div id="dlgPickItem">
        </div>

		<script type="text/javascript">

            function onCompleteHandler(response, status, xhr ) {
                if ( status == "error" ) {
                    var msg = "Sorry, there was an error: ";
                    $( "#dlgPickItem" ).html( msg + xhr.status + " " + xhr.statusText );
                }
            }

            function scrollToAnchor(aid){
                var anchorTag = $("a[name='"+ aid +"']");
                if (anchorTag) {
                    var offset = anchorTag.offset();
                    if (offset) {
                        $('html,body').animate({scrollTop: offset.top}, 'fast');
                    }
                }
            }



			$(function() {


                $(".clickable-row").click(function() {
                    window.location = $(this).data("href");
                });


                $('#tableBinLocations tr').click(function() {
                    $(this).find('td input:radio').prop('checked', true);
                })

                $(":checkbox.checkAll").change(function () {
                    $(":checkbox.shipment-item").prop('checked', $(this).prop("checked"));
                });
                $("#dlgPickItem").dialog({
                    autoOpen: false,
                    modal: true,
                    width: 1000,
                    position: ['center',50]

                });

                $(".btnPickItem").click(function(event){
                    var id = $(this).data("id");
                    var executionKey = $(this).data("execution");
                    console.log(executionKey);
                    var url = "${request.contextPath}/shipmentItem/pick/" + id + "?execution=" + executionKey;
                    $("#dlgPickItem").load(url, onCompleteHandler).dialog("open");
                });

                $(".btnSplitItem").click(function(event){
                    var id = $(this).data("id");
                    var executionKey = $(this).data("execution");
                    console.log(executionKey);
                    var url = "${request.contextPath}/shipmentItem/split/" + id + "?execution=" + executionKey;
                    $("#dlgPickItem").load(url, onCompleteHandler).dialog("open");
                });

                $("#pickShipmentItemBox").draggable();

                //on window scroll fire it will call a function.
                $(window).scroll(function () {
                    //after window scroll fire it will add define pixel added to that element.

                    var documentTop = $(document).scrollTop();
                    var picklistPosition = $("#picklistItemsBox").offset();

                    // Prevent the dialog from moving beyond the top of the picklist
                    if (documentTop <= picklistPosition.top) {
                        documentTop = picklistPosition.top
                    }

                    var dialogTop = (documentTop) + "px";

                    //console.log("offset: ", $("#picklistItemsBox").offset());
                    //console.log("scroll: ", $("#picklistItemsBox").scroll());
                    //console.log("scrollTop: ", $("#picklistItemsBox").scrollTop());

                    //this is the jQuery animate function to fixed the div position after scrolling.
                    $('#pickShipmentItemBox').animate({top:dialogTop},{duration:500,queue:false});
                });

                var id = $("#currentShipmentItemId").val();
                console.log(id);
                if (id) {
                    scrollToAnchor("shipmentItem-" + id);
                }

                $("#quantity").focus().select();

            });
		</script>
		
	</body>
</html>
