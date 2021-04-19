<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'paymentTerms.label', default: 'Payment Terms')}" />
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
            <g:hasErrors bean="${paymentTerm}">
	            <div class="errors">
	                <g:renderErrors bean="${paymentTerm}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list">
                	<img src="${resource(dir: 'images/icons/silk', file: 'table.png')}" />&nbsp;
					<warehouse:message code="default.list.label" args="[entityName]"/>
				</g:link>
				<g:link class="button" action="create">
                	<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
					<warehouse:message code="default.add.label" args="[entityName]"/>
				</g:link>
			</div>

			<g:form action="save" method="post" >
				<div class="box">
					<h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
					<table>
						<tbody>
							<tr class="prop">
								<td valign="middle" class="name">
									<label><warehouse:message code="default.code.label" /></label>
								</td>
								<td valign="middle" class="value ${hasErrors(bean: paymentTerm, field: 'code', 'errors')}">
									<g:textField class="text" size="100" name="code" value="${paymentTerm?.code}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="middle" class="name">
									<label><warehouse:message code="default.name.label" /></label>
								</td>
								<td valign="middle" class="value ${hasErrors(bean: paymentTerm, field: 'name', 'errors')}">
									<g:textField class="text" size="100" name="name" value="${paymentTerm?.name}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="middle" class="name">
									<label><warehouse:message code="default.description.label" /></label>
								</td>
								<td valign="middle" class="value ${hasErrors(bean: paymentTerm, field: 'description', 'errors')}">
									<g:textField class="text" size="100" name="description" value="${paymentTerm?.description}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="middle" class="name">
									<label><warehouse:message code="paymentTerm.prepaymentPercent.label" /></label>
								</td>
								<td valign="middle" class="value ${hasErrors(bean: paymentTerm, field: 'prepaymentPercent', 'errors')}">
									<g:textField class="text" size="100" name="prepaymentPercent" placeholder="e.g. 50" value="${paymentTerm.prepaymentPercent}"/>
								</td>
							</tr>
							<tr class="prop">
								<td valign="middle" class="name">
									<label><warehouse:message code="paymentTerm.daysToPayment.label" /></label>
								</td>
								<td valign="middle" class="value ${hasErrors(bean: paymentTerm, field: 'daysToPayment', 'errors')}">
									<g:textField class="text" size="100" name="daysToPayment" placeholder="e.g. 30" value="${paymentTerm.daysToPayment}"/>
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
    </body>
</html>
