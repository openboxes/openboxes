<table>
	<thead>
		<tr>
			<th>
				<warehouse:message code="default.actions.label"/>
			</th>
			<g:sortableColumn property="status"
				title="${warehouse.message(code: 'default.status.label', default: 'Status')}" />
				
			<g:sortableColumn property="description"
				title="${warehouse.message(code: 'default.description.label', default: 'Description')}" />
				
			<g:if test="${requestType == 'INCOMING' }">
				<g:sortableColumn property="origin"
					title="${warehouse.message(code: 'default.origin.label', default: 'Origin')}" />
			</g:if>
			<g:if test="${requestType == 'OUTGOING' }">
				<g:sortableColumn property="destination"
					title="${warehouse.message(code: 'default.destination.label', default: 'Destination')}" />
			</g:if>
			<th>
				<warehouse:message code="requisition.requisitionItems"/>
			</th>
			<g:sortableColumn property="createdBy"
				title="${warehouse.message(code: 'default.createdBy.label', default: 'Created by')}" />
			
			<g:sortableColumn property="lastUpdated"
				title="${warehouse.message(code: 'default.lastUpdated.label', default: 'Last updated')}" />

		</tr>
	</thead>
	<tbody>
	
	
        <g:unless test="${requests}">
           	<tr class="prop odd">
           		<td colspan="6" class="center">
           			<warehouse:message code="requisition.noRequisitionsMatchingCriteria.message"/>
	           	</td>
			</tr>     
		</g:unless>	
		<g:each in="${requests}" status="i" var="requestInstance">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td>			
					<g:render template="/requisition/actions" model="[requestInstance:requestInstance]"/>
				</td>
				<td>
					<format:metadata obj="${requestInstance?.status}"/>
				</td>
				<td>
					<g:link action="edit" id="${requestInstance.id}">
						${fieldValue(bean: requestInstance, field: "name")}
					</g:link>
				</td>
				<g:if test="${requestType == 'INCOMING' }">
					<td>
						${fieldValue(bean: requestInstance, field: "origin.name")}
					</td>
				</g:if>
				<g:if test="${requestType == 'OUTGOING' }">
					<td>
						${fieldValue(bean: requestInstance, field: "destination.name")}
					</td>
				</g:if>

				<td>${requestInstance?.requisitionItems?.size() }</td>
				<td>${requestInstance.requestedBy}</td>

				<td><format:datetime obj="${requestInstance.lastUpdated}" /></td>


			</tr>
		</g:each>
	</tbody>
</table>
