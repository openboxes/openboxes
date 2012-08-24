<label>
	<g:if test="${container?.parentContainer}">${container?.parentContainer?.name } &rsaquo;</g:if>
	<g:if test="${container?.name }">${container?.name }</g:if>
	<g:else><warehouse:message code="shipping.unpacked.label"/></g:else>
</label>
<div class="fade">
	<g:if test="${container?.weight || container?.width || container?.length || container?.height}">
		<g:if test="${container?.weight}">
			${container?.weight} ${container?.weightUnits}
		</g:if>
		<g:if test="${container?.height && container?.width && container?.length }">
		${container.height} ${container?.volumeUnits}
		| ${container.width} ${container?.volumeUnits}
		| ${container.length} ${container?.volumeUnits}
		</g:if>
	</g:if>
</div>	