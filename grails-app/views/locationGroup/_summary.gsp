<div id="location-group-summary" class="summary">

	<g:if test="${locationGroupInstance?.id}">
        <div class="title">${locationGroupInstance?.name}</div>
	</g:if>
	<g:else>
        <div class="title"><g:message code="default.create.label" args="[g.message(code: 'locationGroup.label')]"/></div>
	</g:else>
</div>