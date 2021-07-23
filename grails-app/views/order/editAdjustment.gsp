<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'default.comment.label', default: 'Comment').toLowerCase()}" />
	<title><warehouse:message code="order.orderAdjustments.label" default="Order Adjustments" /></title>
</head>

<body>

	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${orderInstance}">
			<div class="errors">
				<g:renderErrors bean="${orderInstance}" as="list" />
			</div>
		</g:hasErrors>
		<g:hasErrors bean="${orderAdjustment}">
			<div class="errors">
				<g:renderErrors bean="${orderAdjustment}" as="list" />
			</div>
		</g:hasErrors>

		<div class="dialog">
			<g:render template="summary" model="[orderInstance:orderInstance]" />
			<div class="box">
				<h2><warehouse:message code="order.orderAjustments.label" default="Order Adjustments"/></h2>
				<g:form name="orderAdjustmentForm" action="saveAdjustment">
					<g:hiddenField name="id" value="${orderAdjustment?.id}" />
					<g:hiddenField id="isAccountingRequired" name="isAccountingRequired" value="${isAccountingRequired}">
					</g:hiddenField>
					<table>
						<tbody>
							<tr class="prop">
								<td valign="top" class="name"><label><g:message code="order.label"/></label></td>
								<td valign="top" class="value ${hasErrors(bean: orderAdjustment, field: 'order', 'errors')}">
									<g:hiddenField id="orderId" name="order.id" value="${orderInstance?.id}" />
									${orderInstance?.orderNumber}
									${orderInstance?.name}

								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><g:message code="order.orderItem.label"/></label></td>
								<td valign="top" class="value ${hasErrors(bean: orderAdjustment, field: 'order', 'errors')}">
									<g:selectOrderItems name="orderItem.id"
														orderId="${orderInstance?.id}"
														value="${orderAdjustment?.orderItem?.id}"
														class="chzn-select-deselect"
														noSelection="['':'']"/>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><g:message code="orderAdjustment.orderAdjustmentType.label"/></label></td>
								<td valign="top" class="value ${hasErrors(bean: orderAdjustment, field: 'orderAdjustmentType', 'errors')}">
									<g:selectOrderAdjustmentTypes name="orderAdjustmentType.id"
														value="${orderAdjustment.orderAdjustmentType?.id}"
														class="chzn-select-deselect"
														noSelection="['':'']"/>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="default.description.label"/></label></td>
								<td valign="top" class="value ${hasErrors(bean: orderAdjustment, field: 'description', 'errors')}">
									<g:textField name="description" id="description" class="large text" value="${orderAdjustment.description}"/>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="orderAdjustment.amount.label"/></label></td>
								<td valign="top" class="value ${hasErrors(bean: orderAdjustment, field: 'amount', 'errors')}">
									<g:textField name="amount" class="large text" value="${orderAdjustment.amount}"/>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="orderAdjustment.percentage.label"/></label></td>
								<td valign="top" class="value ${hasErrors(bean: orderAdjustment, field: 'percentage', 'errors')}">
									<g:textField name="percentage" class="large text" value="${orderAdjustment.percentage}"/>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="default.comments.label"/></label></td>
								<td valign="top" class="value ${hasErrors(bean: orderAdjustment, field: 'comments', 'errors')}">
									<g:textArea name="comments" class="large text" value="${orderAdjustment?.comments }"/>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="orderAdjustment.budgetCode.label"/></label></td>
								<td valign="top" class="value">
									<g:selectBudgetCode name="budgetCode"
														id="budgetCode"
														value="${orderAdjustment.budgetCode?.id}"
														class="select2"
														noSelection="['':'']"/>
								</td>
							</tr>
						</tbody>
					</table>
					<div class="buttons">
						<button type="button" class="button icon approve" onclick="saveOrderAdjustment()">
							<warehouse:message code="default.button.save.label"/></button>
						<g:link controller="order" action="show" id="${orderInstance?.id}" class="button icon trash">
							<warehouse:message code="default.button.cancel.label"/></g:link>
					</div>

				</g:form>
			</div>
		</div>
	</div>
<script type="text/javascript">
	function validateForm() {
		var budgetCode = $("#budgetCode").val();
		var description = $("#description").val();
		var isAccountingRequired = ($("#isAccountingRequired").val() === "true");
		if (!budgetCode && isAccountingRequired) {
			$("#budgetCode").notify("Required");
			return false
		} else if (!description) {
          $("#description").notify("Description required");
          return false
		} else {
			return true
		}
	}

	function saveOrderAdjustment() {
		var data = $("#orderAdjustmentForm").serialize();
		if (validateForm()) {
			$.ajax({
				url:'${g.createLink(controller:'order', action:'saveAdjustment')}',
				data: data,
				success: function() {
					$.notify("Successfully saved new adjustment", "success");
					window.location = '${g.createLink(controller: 'purchaseOrder', action: 'addItems', params: [id: orderInstance?.id, skipTo: 'adjustments'])}';
				},
				error: function(jqXHR, textStatus, errorThrown) {
					if (jqXHR.responseText) {
                      try {
                        let data = JSON.parse(jqXHR.responseText);
                        $.notify(data.errorMessage, "error");
                      } catch (e) {
                        $.notify(jqXHR.responseText, "error");
                      }
					} else {
						$.notify("Error saving adjustment");
					}
				}
			});
		} else {
			$.notify("Please enter a value for all required fields");
		}
		return false;
	}
</script>
</body>
</html>
