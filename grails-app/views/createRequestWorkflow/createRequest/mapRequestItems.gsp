<%@page import="java.text.Format"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="request.addRequestItems.label"/></title>
</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${requestInstance}">
			<div class="errors">
				<g:renderErrors bean="${requestInstance}" as="list" />
			</div>
		</g:hasErrors>
		
		<div class="dialog">
			<g:render template="/requisition/summary" model="[requestInstance:requestInstance]"/>
			<g:render template="header" model="['state':'mapRequestItems']"/>
			<g:form action="createRequest" autocomplete="false">
				<div>
					<table>
						<thead>
							<tr class="odd">
								<th width="1%"></th>
								<th width="1%">${warehouse.message(code:'default.quantity.label')}</th>
								<th class="right">${warehouse.message(code:'request.requestItem.label')}</th>
								<th></th>
								<th>${warehouse.message(code:'request.pickItem.label')}</th>

							</tr>
						</thead>
						<tbody>
						
							<g:if test="${requestInstance?.requestItems }">
								<g:set var="i" value="${0 }"/>
								<g:each var="requestItem" in="${requestInstance?.requestItems.sort { it.dateCreated }.reverse() }">
									<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
										<g:hiddenField name="requestItems[${i }].request.id" value="${requestItem?.request?.id }" size="5"/>
										<td>
											<g:if test="${requestItem?.type }">
												<img src="${createLinkTo(dir:'images/icons/type',file: requestItem?.type + '.png')}"/>
											</g:if>
											<g:else>
												<img src="${createLinkTo(dir:'images/icons/silk',file: 'page_white.png')}"/>
											</g:else>									
										</td>
										<td class="center">
											${requestItem?.quantity }
											<g:hiddenField name="requestItems[${i }].quantity" value="${requestItem?.quantity }" size="5"/>
										</td>
										<td class="right middle">
											<format:metadata obj="${requestItem.displayName()}"/>
											<g:if test="${requestItem?.product }">
												<soan class="fade">${requestItem?.product?.unitOfMeasure?:"each" }</soan>
											</g:if>
										</td>
										<td class="center middle">
											<img src="${createLinkTo(dir:'images/icons/silk',file: 'arrow_ew.png')}"/>
										</td>
										<td>
											<g:mapRequestItem requestItem="${requestItem }" status="${i }"/>
										
										</td>
									</tr>
									<g:set var="i" value="${i+1 }"/>
								</g:each>
							</g:if>
							<g:else>
								<tr>
									<td colspan="4" class="center">
										<span class="fade">${warehouse.message(code: 'request.noRequestItems.message') }</span>
									</td>
								</tr>
							</g:else>
						</tbody>								
					</table>
				</div>

			
				<div class="buttons" style="border-top: 1px solid lightgrey;">
					<g:submitButton name="back" value="${warehouse.message(code:'default.button.back.label')}"></g:submitButton>
					<g:submitButton name="next" value="${warehouse.message(code:'default.button.next.label')}"></g:submitButton>
					<g:link action="createRequest" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
				</div>
			</g:form>
				

		</div>

	</div>
	
        <script>

			$(document).ready(function(){

				$("#add-item-quantity").focus();
                
            });
        </script>

</body>
</html>