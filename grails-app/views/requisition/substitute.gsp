<%@ page
	import="grails.converters.JSON; org.pih.warehouse.core.RoleType"%>
<%@ page import="org.pih.warehouse.requisition.RequisitionType"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta name="layout" content="custom" />
<g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
<title><warehouse:message code="${requisition?.id ? 'default.edit.label' : 'default.create.label'}" args="[entityName]" /></title>
<content tag="pageTitle"> <warehouse:message code="${requisition?.id ? 'default.edit.label' : 'default.create.label'}" args="[entityName]" /></content>
<script src="${createLinkTo(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript"></script>
<script src="${createLinkTo(dir:'js/', file:'knockout_binding.js')}" type="text/javascript"></script>
<script src="${createLinkTo(dir:'js/', file:'requisition.js')}" type="text/javascript"></script>
</head>
<body>

	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<g:hasErrors bean="${requisitionItem}">
		<div class="errors">
			<g:renderErrors bean="${requisitionItem}" as="list" />
		</div>
	</g:hasErrors>	

	<g:form name="requisitionSubstituteForm" method="post" action="saveSubstitution">
		<div class="dialog box  ui-validation">
			<div id="requisition-header">
				<div class="title" data-bind="html: requisition.name"></div>
				<div class="time-stamp fade"
					data-bind="html:requisition.lastUpdated"></div>
				<div class="status fade" data-bind="html: requisition.status"></div>
			</div>
		</div>
		<input type="hidden" data-bind="value: requisition.id" />
		<div class="center">
			<input type="submit" id="save-requisition" class="button"
				value="${warehouse.message(code: 'default.button.submit.label')}" />
			<g:link action="${requisition?.id ? 'show': 'list'}"
				id="${requisition?.id}">
				<input type="button" class="button" id="cancelRequisition" name="cancelRequisition"
					value="${warehouse.message(code: 'default.button.cancel.label')}" />
			</g:link>

		</div>

	</g:form>

	<script type="text/javascript">
    $(function () {

    	// Hack to make the requisition type in the name more pretty (need to internationalize this)
		var requisitionTypes = new Object();
		requisitionTypes['ADHOC'] = 'Adhoc';
		requisitionTypes['STOCK'] = 'Stock';
		requisitionTypes['NON_STOCK'] = 'Non Stock';

        var requisitionFromServer = ${requisition.toJson() as JSON};
        var requisitionFromLocal = openboxes.requisition.getRequisitionFromLocal(requisitionFromServer.id);
        var requisitionData = openboxes.requisition.Requisition.getNewer(requisitionFromServer, requisitionFromLocal);
        var viewModel = new openboxes.requisition.EditRequisitionViewModel(requisitionData);
        var requisitionId = viewModel.requisition.id();
        viewModel.savedCallback = function(){
            if(!requisitionId) {
                window.location = "${request.contextPath}/requisition/edit/" + viewModel.requisition.id();
            } else {
                window.location = "${request.contextPath}/requisition/show/" + viewModel.requisition.id();
            }
        };
        ko.applyBindings(viewModel);

        $("#requisitionForm").validate({
            submitHandler: viewModel.save,
            rules:  {
                product: { required: true },
                quantity: { required: true, min: 1 },
                requestedBy: { required: true }
            },
            messages: {
                product: { required: "${warehouse.message(code: 'inventoryItem.productNotSupported.message')}" },
                requestedBy: { required: "${warehouse.message(code: 'person.notFound.message')}" }
            }
        });

        if (!viewModel.requisition.name())
            viewModel.requisition.name("" + requisitionTypes[viewModel.requisition.type()] + " " + "${warehouse.message(code: 'requisition.label')}");

        var updateDescription = function () {
            var type = requisitionTypes[viewModel.requisition.type()] + " ";
            var depot = $("select#depot option:selected").text() || "";
            var program = $("#recipientProgram").val() || "";
            var requestedBy = $("#requestedBy").val() || "";
            var dateRequested = $("#dateRequested").val() || "";
            var description = type + "${warehouse.message(code: 'requisition.label', default: 'Requisition')}";
            if(depot) {
                description += " - " + depot;
            }
            if(program != "") {
                description += " - " + program;
            }
            if(requestedBy != "") {
                description += " - " + requestedBy;
            }
            description += " - " + dateRequested;
            viewModel.requisition.name(description);
        };

        $(".value").change(updateDescription);
        setInterval(function () {
            openboxes.requisition.saveRequisitionToLocal(viewModel.requisition);
        }, 3000);

        $("#cancelRequisition").click(function() {
            if(confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}')) {
                openboxes.requisition.deleteRequisitionFromLocal(requisitionFromServer.id);
                return true;
            }
        });

        $("input.quantity").keyup(function(){
           this.value=this.value.replace(/[^\d]/,'');      
           $(this).trigger("change");//Safari and IE do not fire change event for us!
        });


        


    });
</script>
</body>
</html>
