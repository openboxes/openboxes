<%@ page import="org.pih.warehouse.core.RoleType" %>
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
  <div class="dialog box">
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
           <input id="recipientProgram" name="recipientProgram" 
              class="autocomplete"
              placeholder="${warehouse.message(code:'requisition.program.label')}"
              data-bind="autocomplete: {source: '${request.contextPath }/json/findPrograms'}, value: requisition.recipientProgram"/>
 
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
            <input type="text" class="required ui_datepicker" max-date="${new Date()}"
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
          <input class="required ui_datepicker" min-date="${new Date().plus(1)}" type="text"
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
            <th class="center">
                ${warehouse.message(code: 'requisitionItem.delete.label')}
            </th>
         </tr>
      <thead>
      <tbody data-bind="foreach: requisition.requisitionItems">
        <tr>
          <td class="list-header">
            <input type="hidden" data-bind="value: productId"/>
            <input type="text"
              placeholder="${warehouse.message(code:'requisition.addItem.label')}"
              class="required autocomplete" 
              data-bind="search_product: {source: '${request.contextPath }/json/searchProduct'}, uniqueName: true" size="50"/>
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
          <td class="center">
            <a href='#' data-bind='click: $root.removeItem'>
               <img src="/openboxes/images/icons/silk/delete.png" alt="Delete item" style="vertical-align: middle">
            </a>
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
   </div>
    <input type="hidden" data-bind="value: requisition.id"/>
    <div class="center">
      <g:isUserInRole roles="[RoleType.ROLE_ADMIN]">
        <g:actionSubmit class="delete" onclick="this.form.action='${createLink(action:'delete')}';" value="Delete" />
      </g:isUserInRole>

      <input type="submit" id="save-requisition" value="${warehouse.message(code: 'default.button.save.label')}"/>
      <g:link action="list">
											${warehouse.message(code: 'default.button.cancel.label')}
			</g:link>
      &nbsp;
      <g:actionSubmit class="process" onclick="this.form.action='${createLink(action:'process')}';" value="Process" />
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
                    var program = $("#recipientProgram").val() || "";
                    var requestedBy = $("#requestedBy-suggest").val() || "";
                    var dateRequested = $("#dateRequested").val() || "";
                    var deliveryDate = $("#requestedDeliveryDate").val() || "";
                    var description = "${warehouse.message(code: 'requisition.label', default: 'Requisition')}: " + depot + " - " + program + ", " + requestedBy + " - " + dateRequested + ", " + deliveryDate;
                    requisition.name(description);
                };
  $(".value").change(updateDescription);

  //Todo: very ugly code below because server give us a "does not exist option" which can be selected. should remove it.
  $("#requestedBy-suggest").bind("selected",function(){
    if($("#requestedBy-value").val() == "null"){
      $("#requestedBy-value").val("");
      $("#requestedBy-suggest").val("");
    }
    updateDescription();
  });
  $("#recipientProgram").bind("selected", updateDescription);

});
</script>
</body>
</html>
