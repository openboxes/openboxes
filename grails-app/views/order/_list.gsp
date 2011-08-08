


<table>
	<thead>
		<tr>
			<th>Actions </th>
			<g:sortableColumn property="description"
				title="${warehouse.message(code: 'default.description.label')}" />
				
			<g:if test="${orderType == 'incoming' }">
				<g:sortableColumn property="origin"
					title="${warehouse.message(code: 'default.origin.label')}" />
			</g:if>
			<g:if test="${orderType == 'outgoing' }">
				<g:sortableColumn property="destination"
					title="${warehouse.message(code: 'default.destination.label')}" />
			</g:if>

			<g:sortableColumn property="createdBy"
				title="${warehouse.message(code: 'default.createdBy.label')}" />
			
			<g:sortableColumn property="lastUpdated"
				title="${warehouse.message(code: 'default.lastUpdated.label')}" />

			<g:sortableColumn property="status"
				title="${warehouse.message(code: 'default.status.label')}" />
				
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
					<format:metadata obj="${orderInstance?.status}"/>
				</td>

			</tr>
		</g:each>
	</tbody>
</table>
