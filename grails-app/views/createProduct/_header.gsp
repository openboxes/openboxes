<div class="wizard-steps"> 
	<div class="${currentState.equals("search")?'active-step':''}">
		<g:link action="create" event="search"><warehouse:message code="product.search.label" default="Search for a product"/></g:link>
	</div>
	<div class="${currentState.equals("results")?'active-step':''}">
		<g:link action="create" event="results"><warehouse:message code="product.results.label" default="View results"/></g:link>
	</div>
	<div class="${currentState.equals("verify")?'active-step':''}">
		<g:link action="create" event="verify"><warehouse:message code="product.choose.label" default="Verify product"/></g:link>
	</div>
	<div class="${currentState.equals("confirm")?'active-step':''}">
		<g:link action="create" event="confirm"><warehouse:message code="product.create.label" default="Confirmation"/></g:link>
	</div>
</div>
<br clear="all"/>