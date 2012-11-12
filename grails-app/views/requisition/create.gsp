

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
       <input data-bind="value: requisition.dateRequested" type="hidden"/>
       <input type="text" class="date-picker" max-date="${new Date()}" id="dateRequested" />

    </div>

    <div>
        <label><warehouse:message code="requisition.requestedDeliveryDate.label"/></label>

        <input data-bind="value: requisition.requestedDeliveryDate" type="hidden"/>
        <input class="date-picker" min-date="${new Date().plus(1)}" type="text" id="requestedDeliveryDate"/>

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
    <div id="items" data-bind="foreach: allItems">
      <div class="requisition-item">
        <input type="hidden" data-bind="value: id"/>
        <label>Product</label>
        <input type="hidden" data-bind="value: productId"/>
        <input type="text" data-bind="search_product: {source: '${request.contextPath }/json/searchProduct'}, attr: {'data-value': productId}"/>
        <label>Quantity</label>
        <input type="text" data-bind="value: quantity"/>
        <label>Substitutable</label>
        <input type="checkbox" data-bind="checked: substitutable"/>
        <label>Recipient</label>
        <input type="text" data-bind="value: recipient"/>
        <span>product:</span><span data-bind="text: productId"/>
      </div>
    </div>
    <input type="hidden" data-bind="value: requisition.id"/>
    <input type="submit"  data-bind='enable: validate()' id="save-requisition" value="Save"/>
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


    var today = $.datepicker.formatDate("dd/M/yy", new Date());
    var tomorrow = $.datepicker.formatDate("dd/M/yy", new Date(new Date().getTime() + 24*60*60*1000));
    var requisition = new warehouse.Requisition("", 0, today, tomorrow, "", "");
    var viewModel = new warehouse.ViewModel(requisition);
    ko.applyBindings(viewModel); // This makes Knockout get to work

    $(".date-picker").each(function(index, element){

       var item =  $(element);
       var minDate= item.attr("min-date");
       var maxDate = item.attr("max-date");
       item.val(item.prev().val());
       item.datepicker({
           minDate: minDate && new Date(minDate),
           maxDate: maxDate && new Date(maxDate), 
           dateFormat:'dd/M/yy',
           buttonImageOnly: true,
           buttonImage: '${request.contextPath}/images/icons/silk/calendar.png'
       });

    });

    $('.date-picker').change(function(event) {
        var picker = $(this);
        try {
            var d = $.datepicker.parseDate('dd/M/yy', picker.val());
            picker.prev().val($.datepicker.formatDate('mm/dd/yy', d));
        } catch(err) {
            picker.val("");
            picker.prev().val("");
        }
        picker.prev().trigger("change");
    });

    ko.bindingHandlers.search_product = {
      init: function (element, params) {
          $(element).autocomplete({
           	  delay: 300,
							minLength: 3,
							dataType: 'json',
							source: function(req, add){
								$.getJSON(params().source, { term: req.term}, function(data) {
									var items = [];
									$.each(data, function(i, item) {
										items.push(item);
									});
									add(items);
								});
							},
							focus: function(event, ui) {
								return false;
							},
							change: function(event, ui) {
								if (!ui.item) {
									$(this).prev().val("");  
									$(this).val("");			
								}
								$(this).prev().trigger("change");
                return false;
							},
							select: function(event, ui) {
								if (ui.item) {
									$(this).prev().val(ui.item.value);
									$(this).val(ui.item.label);
								}
								$("#${id}-suggest").trigger("selected");
							  $(this).prev().trigger("change");
                return false;
							}
          })
          .data("autocomplete" )._renderItem = function( ul, item ) {
						    var li = $("<li>").data("item.autocomplete", item );
                if(item.type == 'Product'){
                    var text = item.quantity == null ? item.label : item.label + " QTY: " + item.quantity;
                    li.append("<a>" + text + "</a>" );
                }else{
                    li.append("<span class='product-group'>" + item.label + "</span>" );
                }
                li.appendTo(ul);
                return li;
          };
      }
    };


});
</script>
</body>
</html>
