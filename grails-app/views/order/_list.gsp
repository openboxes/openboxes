


<table>
	<thead>
		<tr>
			<th>Actions </th>
			<g:sortableColumn property="description"
				title="${warehouse.message(code: 'order.description.label', default: 'Description')}" />
				
			<g:if test="${orderType == 'incoming' }">
				<g:sortableColumn property="origin"
					title="${warehouse.message(code: 'order.origin.label', default: 'Origin')}" />
			</g:if>
			<g:if test="${orderType == 'outgoing' }">
				<g:sortableColumn property="destination"
					title="${warehouse.message(code: 'order.destination.label', default: 'Destination')}" />
			</g:if>

			<g:sortableColumn property="createdBy"
				title="${warehouse.message(code: 'order.createdBy.label', default: 'Created by')}" />
			
			<g:sortableColumn property="lastUpdated"
				title="${warehouse.message(code: 'order.lastUpdated.label', default: 'Last updated')}" />

			<g:sortableColumn property="status"
				title="${warehouse.message(code: 'order.status.label', default: 'Status')}" />
				
		</tr>
	</thead>
	<tbody>
		<g:each in="${orderInstanceList}" status="i" var="orderInstance">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td>			
					<g:render template="/order/actions" model="[orderInstance:orderInstance,hideDelete:true]"/> 
				</td>
				<td>
					<g:link action="show" id="${orderInstance.id}">
						${fieldValue(bean: orderInstance, field: "description")}
					</g:link>
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

				<td>${orderInstance.orderedBy}</td>

				<td><format:datetime obj="${orderInstance.lastUpdated}" /></td>

				<td>
					${orderInstance?.status() }
				</td>

			</tr>
		</g:each>
	</tbody>
</table>
