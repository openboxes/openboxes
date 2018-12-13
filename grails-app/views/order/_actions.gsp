<g:if test="${orderInstance?.id }">
	<span id="shipment-action-menu" class="action-menu">
		<button class="action-btn">
			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
		</button>
		
		<g:if test="${orderInstance?.isReceived() }">
			<g:render template="/order/actionsReceived" model="[orderInstance:orderInstance]"/>
		</g:if>
		<g:elseif test="${orderInstance?.isPlaced() }">
			<g:render template="/order/actionsPlaced" model="[orderInstance:orderInstance]"/>
		</g:elseif>
		<g:elseif test="${orderInstance?.isPending() }">
			<g:render template="/order/actionsPending" model="[orderInstance:orderInstance]"/>
		</g:elseif>
		<g:else>
			<g:render template="/order/actionsReceived" model="[orderInstance:orderInstance]"/>
		</g:else>
	</span>
</g:if>