
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'request.label', default: 'Request')}" />
        <title><warehouse:message code="default.fulfill.label" default="Fulfill {0}" args="[entityName]" /></title>

    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
			<g:hasErrors bean="${requestInstance}">
				<div class="errors">
					<g:renderErrors bean="${requestInstance}" as="list" />
				</div>
			</g:hasErrors>
            <div class="dialog">
            	<g:form action="fulfill" method="POST">
            		<g:hiddenField name="id" value="${requestInstance?.id }"/>

	            	<fieldset>
	            		<g:render template="summary" model="[requestInstance:requestInstance]"/>
		                <table>
		                    <tbody>
		                        <g:each var="requestItem" in="${requestInstance?.requisitionItems }">
									<tr class='prop'>
										<td valign='top'>
											<format:metadata obj="${requestItem.displayName()}"/>
										</td>
										<td valign='top'>
											<div class="ui-widget">
												<g:select class="comboBox" name="shipment.id" from="${org.pih.warehouse.shipping.Shipment.list()}"
													optionKey="id" optionValue="name" value="" noSelection="['':'']" />
											</div>
										</td>
									</tr>
								</g:each>
								<tr>
									<td class="name">
									</td>
									<td>
										<div class="buttons">
											<g:submitButton name="${warehouse.message(code:'default.button.submit.label')}"/>
										</div>
									</td>
								</tr>
		                    </tbody>
		                </table>
	               </fieldset>
				</g:form>
            </div>
        </div>
    </body>
</html>
