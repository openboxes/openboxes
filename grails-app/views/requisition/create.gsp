

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>create requisition</title>
    <meta name="layout" content="custom" />
    <script src="${createLinkTo(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript" ></script>
    <script src="${createLinkTo(dir:'js/', file:'requisition.js')}" type="text/javascript" ></script>

</head>
<body>
<g:form name="requisitionForm" method="post" action="save" data-bind='submit: save'>
    <div>
    <label for="origin.id">requesting depot:</label>
    <g:select name="origin.id" from="${depots}"   data-bind="value: requisition.origin_id"
              optionKey="id" optionValue="name" value="${requisition?.origin?.id}" noSelection="['null':'']" />
    </div>
    <div>
       <label><warehouse:message code="requisition.dateRequested.label"/></label>
       <input type="text" class="date-picker" max-date="${new Date()}" id="dateRequested" data-bind="value: requisition.dateRequested" />

    </div>

    <div>
        <label><warehouse:message code="requisition.requestedDeliveryDate.label"/></label>

        <input class="date-picker" min-date="${new Date().plus(1)}" type="text" id="requestedDeliveryDate" data-bind="value: requisition.requestedDeliveryDate" />

    </div>
    <div>
        <label>requested by:</label>
        <g:autoSuggest id="requestedBy" name="requestedBy"
                       dataBind="value: requisition.requestedBy_id"
                       jsonUrl="${request.contextPath }/json/findPersonByName"
                       styleClass="text"
                       placeholder="Requested by"
                       valueId="${requisition?.requestedBy?.id}"
                       valueName="${requisition?.requestedBy?.name}"
                       postSelected="updateDescription();"/>
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
    <input type="submit"  data-bind='enable: validate()' id="save-requisition" value="Save"/>
</g:form>

<div>
    <p>Origin: <span data-bind='text: requisition.origin'></span></p>
    <p>Date Requested:  <span data-bind='text: requisition.dateRequested'></span></p>
    <p>Requested Delivery Date: <span data-bind='text: requisition.requestedDeliveryDate'></span> </p>
    <p>Requested by: <span data-bind='text: requisition.requestedBy'></span> </p>
    <p>program: <span data-bind='text: requisition.program'></span> </p>
</div>
<script type="text/javascript">
  $(function(){


    var today = $.datepicker.formatDate("dd/M/yy", new Date());
    var tomorrow = $.datepicker.formatDate("dd/M/yy", new Date(new Date().getTime() + 24*60*60*1000));
    var requisition = new warehouse.Requisition("", 0, today, tomorrow, "", "");
    var viewModel = new warehouse.ViewModel(requisition);
    ko.applyBindings(viewModel); // This makes Knockout get to work

    $(".date-picker").each(function(index, element){

       var item =  $(element);
       var minDate= item.attr("min-date");
       var maxDate = item.attr("max-date");

       item.datepicker({
           minDate: minDate && new Date(minDate),
           maxDate: maxDate && new Date(maxDate), dateFormat:'dd/M/yy',
           buttonImageOnly: true,
           buttonImage: '${request.contextPath}/images/icons/silk/calendar.png'
       });

    });

    $('.date-picker').change(function() {
        var picker = $(this);
        try {
            var d = $.datepicker.parseDate('dd/M/yy', picker.val());
        } catch(err) {
            picker.val('').trigger("change");
        }
    });


});
</script>
</body>
</html>
