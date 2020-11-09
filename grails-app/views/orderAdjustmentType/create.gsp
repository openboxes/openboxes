
<%@ page import="org.pih.warehouse.order.OrderAdjustmentType" %>
<%@ page import="org.pih.warehouse.core.RoleType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'orderAdjustmentType.label', default: 'Order Adjustment Type')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
		<style>
			.chosen-container > a, .chosen-drop {
				max-width: 500px;
			}
		</style>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${orderAdjustmentType}">
	            <div class="errors">
	                <g:renderErrors bean="${orderAdjustmentType}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list">
                	<img src="${resource(dir: 'images/icons/silk', file: 'table.png')}" />&nbsp;
					<warehouse:message code="default.list.label" args="['Order Adjustment Type']"/>
				</g:link>
				<g:link class="button" action="create">
                	<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
					<warehouse:message code="default.add.label" args="['Order Adjustment Type']"/>
				</g:link>
			</div>

			<g:hiddenField id="isAccountingRequired" name="isAccountingRequired" value="${locationInstance?.isAccountingRequired()}"/>

			<g:form action="save" onsubmit="return validateForm();">
				<div class="box">
					<h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
					<table>
						<tbody>

							<tr class="prop">
								<td valign="middle" class="name">
									<label for="name">
										<warehouse:message code="orderAdjustmentType.name.label" default="Name" />
									</label>
								</td>
								<td valign="middle" class="value">
									<g:textField class="text" size="80" name="name" value="${orderAdjustmentType?.name}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="middle" class="name">
									<label for="description">
										<warehouse:message code="orderAdjustmentType.description.label" default="Description" />
									</label>
								</td>
								<td valign="middle" class="value ${hasErrors(bean: orderAdjustmentType, field: 'description', 'errors')}">
									<g:textField class="text" size="80" name="description" value="${orderAdjustmentType?.description}" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="code">
										<warehouse:message code="orderAdjustmentType.code.label" default="Order Adjustment Type Code" />
									</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: orderAdjustmentType, field: 'code', 'errors')}">
									<g:select name="code" from="${org.pih.warehouse.order.OrderAdjustmentTypeCode?.values()}"
											  value="${orderAdjustmentType?.code}" noSelection="['': '']" class="chzn-select-deselect"/>
								</td>
							</tr>

							<tr class="prop">
								<td class="name middle">
									<label id="glAccountLabel" for="glAccount.id">
										<warehouse:message code="orderAdjustmentType.glAccount.label" default="GL Account"/>
									</label>
								</td>
								<td class="value middle ${hasErrors(bean: orderAdjustmentType, field: 'glAccount', 'errors')}">
									<g:selectGlAccount name="glAccount.id"
													   id="glAccount"
													   value="${orderAdjustmentType?.glAccount?.id}"
													   noSelection="['null':'']"
													   class="chzn-select-deselect" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="middle"></td>
								<td valign="middle">
									<div class="buttons left">
									   <g:submitButton name="create" class="button" value="${warehouse.message(code: 'default.button.create.label', default: 'Create')}" />
									   <g:link action="list">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
            </g:form>
        </div>
		<script type="text/javascript">
			function validateForm()  {
				var glAccount = $("#glAccount").val();
				var isAccountingRequired = ($("#isAccountingRequired").val() === "true");
				if (isAccountingRequired && (!glAccount || glAccount === "null")) {
					$("#glAccountLabel").notify("Required");
					return false;
				} else {
					return true;
				}
			}
		</script>
	</body>
</html>
