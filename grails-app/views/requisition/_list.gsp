<table>
	<thead>
		<tr>
			<th>
				<warehouse:message code="default.actions.label"/>
			</th>
			<g:sortableColumn property="status"
				title="${warehouse.message(code: 'default.status.label', default: 'Status')}" />

            <g:sortableColumn property="requestNumber"
                              title="${warehouse.message(code: 'requisition.requestNumber.label', default: 'Request number')}" />

            <%--
            <g:sortableColumn property="type"
                              title="${warehouse.message(code: 'default.type.label', default: 'Type')}" />

            <g:sortableColumn property="commodityClass"
                              title="${warehouse.message(code: 'requisition.commodityClass.label', default: 'Commodity class')}" />
            --%>
			<g:sortableColumn property="description"
				title="${warehouse.message(code: 'default.description.label', default: 'Description')}" />
			<th>
				<warehouse:message code="default.numItems.label"/>
			</th>
			<g:sortableColumn property="createdBy"
				title="${warehouse.message(code: 'default.createdBy.label', default: 'Created by')}" />
			
			<g:sortableColumn property="lastUpdated"
				title="${warehouse.message(code: 'default.dateCreated.label', default: 'Date created')}" />

		</tr>
	</thead>
	<tbody>
        <g:unless test="${requisitions}">
           	<tr class="prop odd">
           		<td colspan="9" class="center">
           			<warehouse:message code="requisition.noRequisitionsMatchingCriteria.message"/>
	           	</td>
			</tr>     
		</g:unless>	
		<g:each in="${requisitions}" status="i" var="requisition">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td>			
					<g:render template="/requisition/actions" model="[requisition:requisition]"/>
				</td>
				<td>
					<label class="status"><format:metadata obj="${requisition?.status}"/></label>
				</td>
                <td>
                    ${requisition.requestNumber }
                </td>
                <%--
                <td>
                    <format:metadata obj="${requisition?.type}"/>
                </td>
                <td>
                    <format:metadata obj="${requisition?.commodityClass?:warehouse.message(code:'default.none.label')}"/>
                </td>
                --%>
				<td>
					<g:link action="show" id="${requisition.id}">						
						${fieldValue(bean: requisition, field: "name")}
					</g:link>
				</td>
				<td class="center">
					<span class="count">${requisition?.requisitionItems?.size()?:0}</span>
                    <%--
                    ${warehouse.message(code: 'requisition.numRequisitionItems.label', args:[requisition?.requisitionItems?.size()?:0]) }
                    --%>
				</td>
				<td>${requisition.requestedBy}</td>

				<td><format:datetime obj="${requisition.dateCreated}" /></td>

			</tr>
		</g:each>
	</tbody>
</table>
