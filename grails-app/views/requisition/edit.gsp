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
<div class="message" id="flash"></div>
<g:form name="requisitionForm" method="post" action="save">
  <div class="dialog box  ui-validation">
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
          <input name="requestedById" data-bind="value: requisition.requestedById" type="hidden"/>
          <input id="requestedBy" name="requestedBy"
            class="autocomplete required"
            placeholder="${warehouse.message(code:'requisition.requestedBy.label')}"
            data-bind="autocompleteWithId: {source: '${request.contextPath }/json/searchPersonByName'}, value: requisition.requestedByName"/>
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
    <table id="requisition-items"  
      class="ui-validation-items"
      data-bind="visible: requisition.requisitionItems().length">
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
            <input type="hidden" data-bind="value: productId, uniqueName: true"/>
            <input type="text"
              placeholder="${warehouse.message(code:'requisition.addItem.label')}"
              class="required autocomplete" 
              data-bind="search_product: {source: '${request.contextPath }/json/searchProduct'}, uniqueName: true, value: productName" size="50"/>
          </td>
          <td  class="list-header">
            <input type="text" class="required number quantity" size="6" 
            data-bind="value: quantity,uniqueName: true"/>
          </td>
          <td  class="center">
            <input type="checkbox" data-bind="checked: substitutable, uniqueName: true">
          </td>
          <td  class="list-header">
            <input type="text" data-bind="value: recipient, uniqueName: true"/>
          </td>
          <td  class="list-header">
            <input type="text" data-bind="value: comment, uniqueName: true" size="50"/>
          </td>
          <td class="center">
            <a href='#' data-bind='click: $root.requisition.removeItem'>
               <img src="/openboxes/images/icons/silk/delete.png" alt="Delete item" style="vertical-align: middle">
            </a>
          </td>
        </tr>
      </tbody>
      <tfoot>
        <tr>
          <td colSpan="6">
            <input type="button" data-bind='click: requisition.addItem' value="${warehouse.message(code:'requisition.addNewItem.label')}"/>
          </td
        ></tr>
      </tfoot>
    </table>
   </div>
    <input type="hidden" data-bind="value: requisition.id"/>
    <div class="center">
        <g:isUserInRole roles="[RoleType.ROLE_ADMIN]">
            <input type="button" id="deleteRequisition" data-bind='visible: requisition.id' value="${warehouse.message(code: 'default.button.delete.label')}"/>
        </g:isUserInRole>
      <input type="submit" id="save-requisition" value="${warehouse.message(code: 'default.button.save.label')}"/>
       <g:link class="list" action="process" data-bind="visible: requisition.id" id="${requisitionId}">
         <input type="button" value="${warehouse.message(code:'requisition.process.label')}"/>
       </g:link>
      <g:link action="cancel" id="${requisitionId}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
		    <input type="button" name="cancelRequisition" value="${warehouse.message(code: 'default.button.cancel.label')}"/>
	   </g:link>

    </div>
  </g:form>
  <g:form name="deleteRequisitionForm" method="post" action="delete">
    <input type="hidden" name="id" value="${requisitionId}" />
  </g:form>



<script type="text/javascript">
    $(function () {
        $("#flash").hide();
        $("#deleteRequisition").click(function(){$("#deleteRequisitionForm").submit();});
        var requisitionFromServer = ${requisition};
        var requisitionFromLocal = openboxes.requisition.getRequisitionFromLocal(requisitionFromServer.id);
        var requisitionData = openboxes.requisition.Requisition.getNewer(requisitionFromServer, requisitionFromLocal);
        var viewModel = new openboxes.requisition.EditRequisitionViewModel(requisitionData);
        var requisitionId = viewModel.requisition.id();
        viewModel.savedCallback = function(){
            if(!requisitionId) window.location = "${request.contextPath}/requisition/edit/" + viewModel.requisition.id();
            $("#flash").text("${warehouse.message(code:'requisition.saved.message')}").show().delay(3000).fadeOut("slow");
        };
        ko.applyBindings(viewModel);

        $("#requisitionForm").validate({ submitHandler: viewModel.save });

        if (!viewModel.requisition.name())
            viewModel.requisition.name("${warehouse.message(code: 'requisition.label')}");

        var updateDescription = function () {
            var depot = $("#depot select option:selected").text() || "";
            var program = $("#recipientProgram").val() || "";
            var requestedBy = $("#requestedBy").val() || "";
            var dateRequested = $("#dateRequested").val() || "";
            var deliveryDate = $("#requestedDeliveryDate").val() || "";
            var description = "${warehouse.message(code: 'requisition.label', default: 'Requisition')}: " + depot + " - " + program + ", " + requestedBy + " - " + dateRequested + ", " + deliveryDate;
            viewModel.requisition.name(description);
        };
        $(".value").change(updateDescription);
        setInterval(function () {
            openboxes.requisition.saveRequisitionToLocal(viewModel.requisition);
        }, 3000);

        $("input.quantity").keyup(function(){
           this.value=this.value.replace(/[^\d]/,'');      
           $(this).trigger("change");//Safari and IE do not fire change event for us!
        });

    });
</script>
</body>
</html>
