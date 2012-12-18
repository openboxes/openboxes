<g:if test="${requisition?.id }">
	<span id="shipment-action-menu" class="action-menu">
		<button class="action-btn">
			<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" />
			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
		</button>
		
		<g:if test="${requisition?.isPending() }">
			<g:render template="actionsPending" model="[requisition:requisition]"/>
		</g:if>
		<g:elseif test="${requisition?.isRequested() }">
			<g:render template="actionsRequested" model="[requisition:requisition]"/>
		</g:elseif>
		<g:else>
			<div class="actions" style="min-width: 300px;">
				<div class="action-menu-item center">
					<a href="#">No actions available for ${requisition?.status }</a>
				</div>
			</div>
		</g:else>
	</span>
</g:if>
