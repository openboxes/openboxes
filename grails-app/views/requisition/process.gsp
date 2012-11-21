<%@ page import="org.pih.warehouse.requisition.Requisition" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <script src="${createLinkTo(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript" ></script>
    <script src="${createLinkTo(dir:'js/knockout/', file:'knockout.mapping-latest.js')}" type="text/javascript" ></script>
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
                <span data-bind="text: productName" class="product-name"></span>
                ${warehouse.message(code: 'requisitionItem.quantityRequested.label')}: <span data-bind="text:quantity" class="quantity"></span>
                ${warehouse.message(code: 'requisitionItem.quantityPicked.label')}: <span data-bind="text:quantityPicked" class="quantityPicked"></span>
                ${warehouse.message(code: 'requisitionItem.quantityRemaining.label')}: <span data-bind="text:quantityRemaining" class="quantityRemaining"></span>
                <div data-bind="css:status"></div>
              </div>
              <div class="accordion-content">
                <span>hi</span>
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




<script type="text/javascript">
    $(function(){
        var data = ${data};
        var viewModel = new openboxes.requisition.ProcessViewModel(data.requisition, data.picklist, data.productInventoryItemsMap);
        ko.applyBindings(viewModel);
        $("#accordion").accordion({header:".accordion-header", active:false, collapsible:true});
    });
</script>

</body>
</html>
