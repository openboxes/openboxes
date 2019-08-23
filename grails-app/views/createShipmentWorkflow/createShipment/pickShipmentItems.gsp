  
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title><warehouse:message code="shipping.pickShipmentItems.label"/></title>
         <style>
         	.top-border { border-top: 2px solid lightgrey; }
         	.right-border { border-right: 2px solid lightgrey; }
             .active { background-color: #f3f8fc;  }
             .active td { color: #666; }
             .same-lot-number { font-weight: bolder; color: #000 }
             .different-product { border-top: 3px solid lightgrey; }
             .body { min-height: 800px; }
         </style>
    </head>
    <body>
        <div class="body">
        	<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
            <g:if test="${message}">
                <div class="message">${message}</div>
            </g:if>

            <g:render template="../shipment/summary" />
            <g:render template="flowHeader" model="['currentState':'Picking']"/>

            <g:set var="shipmentItemsSorted" value="${shipmentInstance.sortShipmentItems()}"/>

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

            <div class="yui-gc">
                <div class="yui-u first">
                    <g:form action="createShipment" method="post">
                        <g:hiddenField name="id" value="${shipmentInstance?.id}"/>
                        <g:hiddenField name="shipment.id" value="${shipmentInstance?.id}"/>
                        <g:hiddenField name="currentShipmentItemId" value="${currentShipmentItemId}"/>
                        <div id="picklistItemsBox" class="dialog box">
                            <h2>
                                <img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}"/>
                                <warehouse:message code="shipping.pickShipmentItems.label"/>
                            </h2>

                            <g:if test="${session.warehouse == shipmentInstance?.origin}">
                                <table id="picklistItemTable" class="table">
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
                                            <g:set var="errors" value="${null}"/>
                                            <g:if test="${errorsMap}">
                                                <g:set var="errors" value="${errorsMap[shipmentItem?.id]}"/>
                                            </g:if>
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

                                            <g:set var="href" value="${g.createLink(controller: 'shipmentItem', action:'pick', id:shipmentItem?.id)}"/>

                                            <tr id="picklist-item-${shipmentItem?.id}" data-href="${href}" data-execution="${params.execution}"
                                                class="clickable-row ${isActive?'active':''} ${status % 2 ? 'even' : 'odd' } ${!isSameAsPreviousContainer ? 'top-border':'' } ${!isSameAsPreviousProduct ? 'different-product':'' }">


                                                <td class="top right-border">
                                                    <a name="picklist-item-${shipmentItem?.id}"></a>
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

                                                    <g:if test="${errors}">
                                                        <span title="${errors}">
                                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'exclamation.png')}" />
                                                        </span>
                                                    </g:if>
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
                                        <g:hasErrors bean="${shipmentInstance}" field="quantity">
                                            <tr>
                                                <td colspan="12">
                                                    <div class="errors">
                                                        <g:renderErrors bean="${shipmentInstance}" as="list" />
                                                    </div>

                                                </td>
                                            </tr>
                                        </g:hasErrors>
                                        <tr>
                                            <td colspan="11" class="right">
                                                <div class="button-container">
                                                <%--
                                                <g:submitButton name="autoPickShipmentItems" value="${g.message(code:'shipping.autoPickItems.label')}" class="button"></g:submitButton>
                                                --%>
                                                    ${hasErrors(bean: shipmentInstance, field: 'quantity')}
                                                    <g:if test="${hasErrors(bean: shipmentInstance, field: 'quantity')}">
                                                        <g:eachError bean="${shipmentInstance}" field="quantity" var="error">
                                                            ${error}
                                                        </g:eachError>
                                                    </g:if>

                                                    <g:isSuperuser>
                                                        <button name="_eventId_clearPicklist" class="button">
                                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'basket_remove.png')}" />&nbsp;
                                                            <warehouse:message code="shipping.clearPicklist.label" default="Clear picklist"/>
                                                        </button>
                                                    </g:isSuperuser>

                                                    <button name="_eventId_validatePicklist" class="button">
                                                        <img src="${createLinkTo(dir:'images/icons/silk',file:'error.png')}" />&nbsp;
                                                        <warehouse:message code="shipping.validatePicklist.label" default="Validate picklist"/>
                                                    </button>

                                                    <div class="button-group">
                                                        <a href="#" class="previous-picklist-item button">
                                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'resultset_previous.png')}" alt="Previous Item"/>&nbsp;
                                                        <g:message code="default.button.back.label" default="Back"/>
                                                        </a>
                                                        <a href="#" class="next-picklist-item button">
                                                            <g:message code="default.button.next.label" default="Next"/>
                                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'resultset_next.png')}" alt="Next Item"/>&nbsp;
                                                        </a>
                                                    </div>
                                                </div>


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

                    <div id="pickShipmentItemBox" style="position: absolute; width: 30%;">
                        <div class="box">
                            <h2>
                                <img src="${createLinkTo(dir:'images/icons',file:'draggable.png')}"
                                     title="${g.message(code:'picklist.picked.label')}">
                                <g:message code="shipping.editPicklistItem.label" default="Edit Picklist Item"/>
                            </h2>

                            <div class="empty fade center">
                                <g:message code="shipping.pickShipmentItems.message" default="Select an item from the picklist"/>
                                <button class="button next-picklist-item">
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'next_blue.png')}" alt="Next Item"/>&nbsp;
                                    <g:message code="default.button.next.label" default="Next"/>
                                </button>
                            </div>
                        </div>


                    </div>
                </div>
            </div>
		</div>

        <div id="dlgPickItem">
        </div>

		<script type="text/javascript">

            var currentPicklistItem

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

            function showPicklistItem(picklistItem) {

                console.log("show picklist item", picklistItem)

                // Repaint table active rows
                $('tr').removeClass('active');

                currentPicklistItem = picklistItem;

                if (!picklistItem) {
                    $("#pickShipmentItemBox").html("<div class='box'><h2>Edit Picklist Item</h2><div class='center fade empty'>Please select a picklist item</div></div>");
                }
                else {
                    var url = picklistItem.data("href");
                    var executionKey = picklistItem.data("execution")

                    // Submit request to server
                    var data = {};
                    $("#pickShipmentItemBox").load(url, data, function () {
                        // callback method
                    });
                    picklistItem.addClass("active");

                    console.log("anchor: ", picklistItem.attr("id"));
                    scrollToAnchor(picklistItem.attr("id"))

                }



            }

            function nextPicklistItem(picklistItem) {
                var nextPicklistItem = picklistItem
                var firstPicklistItem = $("#picklistItemTable tbody tr:first");
                var lastPicklistItem = $("#picklistItemTable tbody tr:last");
                var isLast = picklistItem.attr("id") == lastPicklistItem.attr("id");
                if (isLast) {
                    nextPicklistItem = firstPicklistItem;
                }
                else {
                    nextPicklistItem = picklistItem.next('tr');
                }
                showPicklistItem(nextPicklistItem)
            }

            function previousPicklistItem(picklistItem) {
                var previousPicklistItem = picklistItem;
                var firstPicklistItem = $("#picklistItemTable tbody tr:first");
                var lastPicklistItem = $("#picklistItemTable tbody tr:last");
                var isFirst = picklistItem.attr("id") == firstPicklistItem.attr("id");
                if (isFirst) {
                    previousPicklistItem = lastPicklistItem;
                }
                else {
                    previousPicklistItem = picklistItem.prev('tr');
                }
                showPicklistItem(previousPicklistItem);
            }


            $(function() {

			    // Handles interaction when user clicks picklist row
                $(".clickable-row").click(function() {

                    if (currentPicklistItem && $(this).attr("id") == currentPicklistItem.attr("id")) {
                        showPicklistItem(null);
                    }
                    else {
                        showPicklistItem($(this));
                    }
                });

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

                    //this is the jQuery animate function to fixed the div position after scrolling.
                    $('#pickShipmentItemBox').animate({top:dialogTop},{duration:500,queue:false});
                });

                var id = $("#currentShipmentItemId").val();
                if (id) {
                    scrollToAnchor("shipmentItem-" + id);
                }

                $("#quantity").livequery(function() {
                    $(this).focus().select();
                });

                $(".next-picklist-item").livequery('click', function(event) {
                    event.preventDefault();
                    nextPicklistItem(currentPicklistItem);
                });

                $(".previous-picklist-item").livequery('click', function(event) {
                    event.preventDefault();
                    previousPicklistItem(currentPicklistItem);
                });



                $('#tableBinLocations tr').livequery('click', function() {
                    $(this).find('td input:radio').prop('checked', true);
                });

                // Show the first picklist item on page load (or the picklist item that was in focus before a server event)
                var picklistItem;
                var currentShipmentItemId = $("#currentShipmentItemId").val();
                if (currentShipmentItemId) {
                    picklistItem = $("#picklist-item-" + currentShipmentItemId);
                }
                else {
                    picklistItem = $("#picklistItemTable tbody tr:first")
                }

                showPicklistItem(picklistItem);

            });
		</script>
		
	</body>
</html>
