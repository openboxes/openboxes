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
		<div class="${state.equals("enterRequestDetails")?'active-step':''}">
			<g:link action="createRequest" event="enterRequestDetails">1. <warehouse:message code="createRequestWorkflow.enterRequestDetails.label" default="Enter request details"/></g:link>
		</div>
		<div class="${state.equals("addRequestItems")?'active-step':''}">
			<g:link action="createRequest" event="addRequestItems">2. <warehouse:message code="createRequestWorkflow.addRequestItems.label" default="Add request items"/></g:link>
		</div>
		<div class="${state.equals("mapRequestItems")?'active-step':''}">
			<g:link action="createRequest" event="mapRequestItems">3. <warehouse:message code="createRequestWorkflow.mapRequestItems.label" default="Map request items"/></g:link>
		</div>		
		<div class="${state.equals("pickRequestItems")?'active-step':''}">
			<g:link action="createRequest" event="pickRequestItems">4. <warehouse:message code="createRequestWorkflow.pickRequestItems.label" default="Pick items"/></g:link>
		</div>
		<div class="${state.equals("printPicklist")?'active-step':''}">
			<g:link action="createRequest" event="printPicklist">5. <warehouse:message code="createRequestWorkflow.printPicklist.label" default="Print picklist"/></g:link>
		</div>
		<div class="${state.equals("confirmPicklist")?'active-step':''}">
			<g:link action="createRequest" event="confirmPicklist">6. <warehouse:message code="createRequestWorkflow.confirmPicklist.label" default="Confirm picklist"/></g:link>
		</div>
	</div>	
</div>

