

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>create requisition</title>
    <meta name="layout" content="custom" />
    <script src="${createLinkTo(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript" ></script>
    <script src="${createLinkTo(dir:'js/', file:'knockout_binding.js')}" type="text/javascript" ></script>
    <script src="${createLinkTo(dir:'js/', file:'requisition.js')}" type="text/javascript" ></script>

</head>
<body>
<g:form name="requisitionForm" method="post" action="save">
    <div>
    <label for="origin.id">requesting depot:</label>
    <g:select name="origin.id" from="${depots}"   data-bind="value: requisition.originId"
              optionKey="id" optionValue="name" class='required' value="${requisition?.origin?.id}" noSelection="['':'']"/>
    </div>
    <div>
       <label><warehouse:message code="requisition.dateRequested.label"/></label>
       <input data-bind="value: requisition.dateRequested" type="hidden"/>
       <input type="text" class="required" max-date="${new Date()}"
          id="dateRequested"
          data-bind="date_picker:{}"/>

    </div>

    <div>
        <label><warehouse:message code="requisition.requestedDeliveryDate.label"/></label>

        <input data-bind="value: requisition.requestedDeliveryDate" type="hidden"/>
        <input class="required" min-date="${new Date().plus(1)}" type="text"
           id="requestedDeliveryDate"
           data-bind="date_picker:{}"/>

    </div>
    <div>
        <label>requested by:</label>
        <g:autoSuggest id="requestedBy" name="requestedBy"
                       dataBind="value: requisition.requestedById"
                       jsonUrl="${request.contextPath }/json/findPersonByName"
                       styleClass="text required"
                       placeholder="Requested by"
                       valueId="${requisition?.requestedBy?.id}"
                       valueName="${requisition?.requestedBy?.name}"
                       />
    </div>
    <div>
        <label>program:</label>
        <g:autoSuggestString id="recipientProgram"
                             dataBind = 'value: requisition.recipientProgram'
                             name="recipientProgram"
                             placeholder="Program"
                             jsonUrl="${request.contextPath }/json/findPrograms"
                             class="text"
                             label="${requisition?.recipientProgram}"/>
    </div>
    <div id="items" data-bind="foreach: requisition.requisitionItems">
      <div class="requisition-item">
        <input type="hidden" data-bind="value: id"/>
        <label>Product</label>
        <input type="hidden" data-bind="value: productId"/>
        <input type="text" class="required" data-bind="search_product: {source: '${request.contextPath }/json/searchProduct'}, uniqueName: true"/>
        <label>Quantity</label>
        <input type="text" class="required number" data-bind="value: quantity,uniqueName: true"/>
        <label>Substitutable</label>
        <input type="checkbox" data-bind="checked: substitutable"/>
        <label>Recipient</label>
        <input type="text" data-bind="value: recipient"/>
        <span>product:</span><span data-bind="text: productId"/>
      </div>
    </div>
    <input type="hidden" data-bind="value: requisition.id"/>
    <input type="submit" id="save-requisition" value="Save"/>
    <button data-bind='click: addItem'>Add New Item</button>
</g:form>

<div>
    <p>Origin: <span data-bind='text: requisition.origin_id'></span></p>
    <p>Date Requested:  <span data-bind='text: requisition.dateRequested'></span></p>
    <p>Requested Delivery Date: <span data-bind='text: requisition.requestedDeliveryDate'></span> </p>
    <p>Requested by: <span data-bind='text: requisition.requestedBy_id'></span> </p>
    <p>program: <span data-bind='text: requisition.recipientProgram'></span> </p>
</div>
<script type="text/javascript">
  $(function(){
    var today = $.datepicker.formatDate("mm/dd/yy", new Date());
    var tomorrow = $.datepicker.formatDate("mm/dd/yy", new Date(new Date().getTime() + 24*60*60*1000));
    var requisition = new warehouse.Requisition("", 0, today, tomorrow, "", "");
    var viewModel = new warehouse.ViewModel(requisition);
    ko.applyBindings(viewModel); // This makes Knockout get to work
    $("#requisitionForm").validate({ submitHandler: viewModel.save });
});
</script>
</body>
</html>
