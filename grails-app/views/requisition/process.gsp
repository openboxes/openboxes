<%@ page import="org.pih.warehouse.requisition.Requisition" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <script src="${createLinkTo(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript" ></script>
    <script src="${createLinkTo(dir:'js/', file:'requisition.js')}" type="text/javascript" ></script>
    <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
    <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    <!-- Specify content to overload like global navigation links, page titles, etc. -->
    <content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
</head>
<body>
<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div id="picklist" class="left">
        <div id="requisition-header">
            <div class="title" id="description" data-bind="html: requisition.name"></div>
            <div class="time-stamp fade" data-bind="text: requisition.lastUpdated"></div>
            <div class="status fade"><span data-bind="text: requisition.status"></span></div>
        </div>

        <g:form name="requisitionForm" method="post" action="saveProcess">
            <div class="dialog">
              <ul id="accordion" data-bind="foreach: requisition.requisitionItems">
                <li>
                  <div class="accordion-header">
                    <div data-bind="text: productName" class="product-name"></div>
                    <div class="quantity">${warehouse.message(code: 'requisitionItem.quantityRequested.label')}: <span data-bind="text:quantity"></span></div>
                    <div class="quantityPicked">
                    ${warehouse.message(code: 'requisitionItem.quantityPicked.label')}: <span data-bind="text:quantityPicked"></span></div>
                    <div class="quantityRemaining">
                    ${warehouse.message(code: 'requisitionItem.quantityRemaining.label')}: <span data-bind="text:quantityRemaining"></span></div>
                    <div class="status"><div data-bind="css:status" class="right requisition-status"></div></div>
                    <div class="clear"></div>
                  </div>
                  <div class="accordion-content">
                    <div class="picklist-header">
                        <div class="product-name"><warehouse:message code="inventoryItem.item.label"/></div>
                        <div class="lot"><warehouse:message code="inventoryItem.lotNumber.label"/></div>
                        <div class="expiration-date"><warehouse:message code="inventoryItem.expirationDate.label"/></div>
                        <div class="quantity-onhand"><warehouse:message code="inventoryItem.onHandQuantity.label"/></div>
                        <div class="quantity-picked"><warehouse:message code="inventoryItem.quantityPicked.label"/></div>
                        <div class="clear"></div>
                    </div>
                    <div class="picklist-items" data-bind="foreach: picklistItems">
                        <div class="product-name" data-bind="text: $parent.productName"></div>
                        <div class="lot" data-bind="text: lotNumber"></div>
                        <div class="expiration-date" data-bind="text: expirationDate"></div>
                        <div class="quantity-onhand" data-bind="text: quantityOnHand"></div>
                        <div class="quantity-picked"><input data-bind="value: quantityPicked"></input></div>
                        <div class="clear"></div>
                    </div>
                  </div>
                </li>   
              </ul>
            
            </div>
            <div class="center">
                <input type="submit" id="save-requisition" value="${warehouse.message(code: 'default.button.save.label')}"/>
                <g:link action="list">
                    ${warehouse.message(code: 'default.button.cancel.label')}
                </g:link>
                &nbsp;
            </div>


        </g:form>
    </div>
    <div class=" right" id="requisition-status-key">
            <fieldset>
                <legend class="left"><warehouse:message code="requisitionItem.legend.label"/></legend>
                <div class="Complete left requisition-status"></div>
                <div class="left"><warehouse:message code="requisitionItem.complete.label"/></div>
                <div class="clear"></div>
                <div class="PartiallyComplete left requisition-status"></div>
                <div class="left"><warehouse:message code="requisitionItem.partiallycomplete.label"/></div>
                <div class="clear"></div>                
                <div class="Incomplete left requisition-status"></div>
                <div class="left"><warehouse:message code="requisitionItem.incomplete.label"/></div>
                <div class="clear"></div>
        </fieldset>
    </div>
    <div class="clear"></div>
</div>




<script type="text/javascript">
    $(function(){
        var data = ${data};
        var viewModel = new openboxes.requisition.ProcessViewModel(data.requisition, data.picklist, data.productInventoryItemsMap);
        ko.applyBindings(viewModel);
        $("#accordion").accordion({header:".accordion-header", heightStyle:"content", icons:false, active:false, collapsible:true});
    });
</script>

</body>
</html>
