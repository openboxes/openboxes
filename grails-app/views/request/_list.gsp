


<table>
	<thead>
		<tr>
			<th> </th>
			<g:sortableColumn property="description"
				title="${message(code: 'request.description.label', default: 'Description')}" />
				
			<g:if test="${requestType == 'incoming' }">
				<g:sortableColumn property="origin"
					title="${message(code: 'request.origin.label', default: 'Origin')}" />
			</g:if>
			<g:if test="${requestType == 'outgoing' }">
				<g:sortableColumn property="destination"
					title="${message(code: 'request.destination.label', default: 'Destination')}" />
			</g:if>

			<g:sortableColumn property="createdBy"
				title="${message(code: 'request.createdBy.label', default: 'Created by')}" />
			
			<g:sortableColumn property="lastUpdated"
				title="${message(code: 'request.lastUpdated.label', default: 'Last updated')}" />

			<g:sortableColumn property="status"
				title="${message(code: 'request.status.label', default: 'Status')}" />
				
		</tr>
	</thead>
	<tbody>
		<g:each in="${requestInstanceList}" status="i" var="requestInstance">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td>			
					<g:render template="/request/actions" model="[requestInstance:requestInstance]"/> 
				</td>
				<td>
					<g:link action="show" id="${requestInstance.id}">
						${fieldValue(bean: requestInstance, field: "description")}
					</g:link>
				</td>
				<g:if test="${requestType == 'incoming' }">
					<td>
						${fieldValue(bean: requestInstance, field: "origin.name")}
					</td>
				</g:if>
				<g:if test="${requestType == 'outgoing' }">
					<td>
						${fieldValue(bean: requestInstance, field: "destination.name")}
					</td>
				</g:if>

				<td>${requestInstance.requestedBy}</td>

				<td><format:datetime obj="${requestInstance.lastUpdated}" /></td>

				<td>
					${requestInstance?.status() }
				</td>

			</tr>
		</g:each>
	</tbody>
</table>
