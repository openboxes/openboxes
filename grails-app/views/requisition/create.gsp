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
    <div id="requisition-header">
                <div class="title" data-bind="html:requisition.name"></div>
                <div class="time-stamp fade" data-bind="html:requisition.lastUpdated"></div>
                <div class="status fade" data-bind="html: requisition.status"></div>
     </div>
    <table id="requisition-body">
      <tr class="prop">
        <td class="name">
          <label for="origin.id"><warehouse:message code="requisition.requestingDepot.label" /></label>
        </td>
    <td class="value">
          <g:select name="origin.id" from="${depots}"
              id = "depot"
              data-bind="value: requisition.originId"
              optionKey="id" optionValue="name" class='required' value=""
              noSelection="['':'']"/>
        </td>
      </tr>
      <tr class="prop">
        <td class="name">
          <label><warehouse:message code="requisition.program.label"/></label>
        </td>
        <td class="value">
          <g:autoSuggestString id="recipientProgram"
                   daVtaBind = 'value: requisition.recipientProgram'
                   name="recipientProgram"
                   placeholder="Program"
                   jsonUrl="${request.contextPath }/json/findPrograms"
                   class="text"
                   label="${requisition?.recipientProgram}"/>
 
        </td>
      </tr>
      <tr class="prop">
        <td class="name">
          <label><warehouse:message code="requisition.requestedBy.label"/></label>
        </td>
        <td class="value">
           <g:autoSuggest id="requestedBy" name="requestedBy"
                     dataBind="value: requisition.requestedById"
                     jsonUrl="${request.contextPath }/json/findPersonByName"
                     styleClass="text required"
                     placeholder="Requested by"
                     valueId="${requisition?.requestedBy?.id}"
                     valueName="${requisition?.requestedBy?.name}"
                     />
        </td>
      </tr>
      <tr class="prop">
        <td class="name">
          <label><warehouse:message code="requisition.dateRequested.label"/></label>
        </td>
        <td class="value">
           <input data-bind="value: requisition.dateRequested" type="hidden"/>
            <input type="text" class="required" max-date="${new Date()}"
              id="dateRequested"
              data-bind="date_picker:{}"/>
        </td>
      </tr>
      <tr class="prop">
        <td class="name">
          <label><warehouse:message code="requisition.requestedDeliveryDate.label"/></label>
        </td>
        <td class="value">
          <input data-bind="value: requisition.requestedDeliveryDate" type="hidden"/>
          <input class="required" min-date="${new Date().plus(1)}" type="text"
           id="requestedDeliveryDate"
           data-bind="date_picker:{}"/>

        </td>
      </tr>
    </table>
    <table id="requisition-items" data-bind="visible: requisition.requisitionItems().length">
      <thead >
        <tr class="prop">
           <th class="list-header">
                ${warehouse.message(code: 'requisitionItem.item.label')}
            </th>
            <th class="list-header">
                ${warehouse.message(code: 'requisitionItem.quantity.label')}
            </th>
            <th class="center">
                ${warehouse.message(code: 'requisitionItem.substitutable.label')}
            </th>
            <th class="list-header">
                ${warehouse.message(code: 'requisitionItem.recipient.label')}
            </th>
            <th class="list-header">
                ${warehouse.message(code: 'requisitionItem.comment.label')}
            </th>
            <th class="list-header">
                ${warehouse.message(code: 'requisitionItem.delete.label')}
            </th>
         </tr>
      <thead>
      <tbody data-bind="foreach: requisition.requisitionItems">
        <tr>
          <td class="list-header">
            <input type="hidden" data-bind="value: productId"/>
            <input type="text" class="required" data-bind="search_product: {source: '${request.contextPath }/json/searchProduct'}, uniqueName: true" size="50"/>
          </td>
          <td  class="list-header">
            <input type="text" class="required number" size="6" 
            data-bind="value: quantity,uniqueName: true"/>
          </td>
          <td  class="center">
            <input type="checkbox" data-bind="checked: substitutable">
          </td>
          <td  class="list-header">
            <input type="text" data-bind="value: recipient"/>
          </td>
          <td  class="list-header">
            <input type="text" data-bind="value: comment" size="50"/>
          </td>
          <td class="list-header">
            <a href='#' data-bind='click: $root.removeItem'>Delete</a>
          </td>
        </tr>
      </tbody>
      <tfoot>
        <tr>
          <td colSpan="6">
            <input type="button" data-bind='click: addItem' value="${warehouse.message(code:'requisition.addNewItem.label')}"/>
          </td
        ></tr>
      </tfoot>
    </table>
    <input type="hidden" data-bind="value: requisition.id"/>
    <div class="center">
      <input type="submit" id="save-requisition" value="${warehouse.message(code: 'default.button.save.label')}
"/>
    </div>
    </g:form>

<script type="text/javascript">
  $(function(){
    var today = $.datepicker.formatDate("mm/dd/yy", new Date());
    var tomorrow = $.datepicker.formatDate("mm/dd/yy", new Date(new Date().getTime() + 24*60*60*1000));
    var requisition = new warehouse.Requisition({ dateRequested: today, requestedDeliveryDate:tomorrow});
    var viewModel = new warehouse.ViewModel(requisition);
    ko.applyBindings(viewModel);
    $("#requisitionForm").validate({ submitHandler: viewModel.save });
    
    if(!requisition.name())
      requisition.name("${warehouse.message(code: 'requisition.label')}");

    var updateDescription = function() {
                    var depot = $("#depot select option:selected").text() || "";
                    var program = $("#autosuggest-recipientProgram").val() || "";
                    var requestedBy = $("#requestedBy-suggest").val() || "";
                    var dateRequested = $("#dateRequested").val() || "";
                    var deliveryDate = $("#requestedDeliveryDate").val() || "";
                    var description = "${warehouse.message(code: 'requisition.label', default: 'Requisition')}: " + depot + " - " + program + ", " + requestedBy + " - " + dateRequested + ", " + deliveryDate;
                    requisition.name(description);
                };
  $(".value").change(updateDescription);

});
</script>
</body>
</html>
