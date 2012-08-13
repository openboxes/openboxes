<g:if test="${message}">
	<div class="message">${message}</div>
</g:if>
<g:hasErrors bean="${command}">
	<div class="errors">
		<g:renderErrors bean="${command}" as="list" />
	</div>				
</g:hasErrors> 				
<g:if test="${flowExecutionException}">
	<div class="errors">
		<ul>
			<li>${flowExecutionException?.message }</li>
		</ul>					
	</div>
</g:if>

<div class="box-white" style="height: 25px; display: block;">
	<div class="wizard-steps"> 
		<div class="${currentState.equals("chooseTemplate")?'active-step':''}">
			<g:link action="create" event="chooseTemplate"><warehouse:message code="productCreateFromTemplate.chooseTemplate.label" default="Choose template"/></g:link>
		</div>
		<div class="${currentState.equals("enterDetails")?'active-step':''}">
			<g:link action="create" event="enterDetails"><warehouse:message code="productCreateFromTemplate.enterDetails.label" default="Enter details"/></g:link>
		</div>
		<div class="${currentState.equals("confirmDetails")?'active-step':''}">
			<g:link action="create" event="confirmDetails"><warehouse:message code="productCreateFromTemplate.confirmDetails.label" default="Confirm details"/></g:link>
		</div>
		<div class="${currentState.equals("showProduct")?'active-step':''}">
			<g:link action="create" event="showProduct"><warehouse:message code="productCreateFromTemplate.showProduct.label" default="Show product"/></g:link>
		</div>
	</div>
</div>
