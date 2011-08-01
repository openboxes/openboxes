<g:if test="${orderInstance?.id }">
	<span id="shipment-action-menu" class="action-menu">
		<button class="action-btn">
			<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" />
			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
		</button>
		
		<g:if test="${orderInstance?.isReceived() || orderInstance?.isCompletelyReceived() }">
			<g:render template="actionsReceived" model="[orderInstance:orderInstance,hideDelete:hideDelete]"/>
		</g:if>
		<g:elseif test="${orderInstance?.isPlaced() }">
			<g:render template="actionsPlaced" model="[orderInstance:orderInstance,hideDelete:hideDelete]"/>
		</g:elseif>
		<g:elseif test="${orderInstance?.isPending() }">
			<g:render template="actionsPending" model="[orderInstance:orderInstance,hideDelete:hideDelete]"/>
		</g:elseif>
		<g:else>
			<div class="actions" style="min-width: 200px;">
				<div class="action-menu-item">
					Unknown state
				</div>
			</div>
		</g:else>
	</span>
</g:if>