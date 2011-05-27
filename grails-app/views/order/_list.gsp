


<table>
	<thead>
		<tr>
			<g:sortableColumn property="orderNumber"
				title="${message(code: 'order.orderNumber.label', default: 'Order Number')}" />

			<g:sortableColumn property="description"
				title="${message(code: 'order.description.label', default: 'Description')}" />
				
			<g:if test="${orderType == 'incoming' }">
				<g:sortableColumn property="origin"
					title="${message(code: 'order.origin.label', default: 'Origin')}" />
			</g:if>
			<g:if test="${orderType == 'outgoing' }">
				<g:sortableColumn property="destination"
					title="${message(code: 'order.destination.label', default: 'Destination')}" />
			</g:if>
			<g:sortableColumn property="lastUpdated"
				title="${message(code: 'order.lastUpdated.label', default: 'Last Updated')}" />
				
			<g:sortableColumn property="status"
				title="${message(code: 'order.status.label', default: 'Status')}" />
		</tr>
	</thead>
	<tbody>
		<g:each in="${orderInstanceList}" status="i" var="orderInstance">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

				<td>
					<g:link action="show" id="${orderInstance.id}">
						<g:if test="${orderInstance?.orderNumber }">
							${fieldValue(bean: orderInstance, field: "orderNumber")}
						</g:if>
						<g:else>
							${fieldValue(bean: orderInstance, field: "id")}
						</g:else>
					</g:link>
				</td>
				<td>
					${fieldValue(bean: orderInstance, field: "description")}
				</td>
				<g:if test="${orderType == 'incoming' }">
					<td>
						${fieldValue(bean: orderInstance, field: "origin.name")}
					</td>
				</g:if>
				<g:if test="${orderType == 'outgoing' }">
					<td>
						${fieldValue(bean: orderInstance, field: "destination.name")}
					</td>
				</g:if>

				<td><g:formatDate date="${orderInstance.lastUpdated}" /></td>

				<td>Pending</td>


			</tr>
		</g:each>
	</tbody>
</table>
