<table>
	<thead>
		<tr>
			<th>
				<warehouse:message code="default.actions.label"/>
			</th>

            <%--
			<g:sortableColumn property="status"
				title="${warehouse.message(code: 'default.status.label', default: 'Status')}" />
				
				
			<g:sortableColumn property="requestNumber"
				title="${warehouse.message(code: 'requisition.requestNumber.label', default: 'Request number')}" />
				
				
			<g:sortableColumn property="description"
				title="${warehouse.message(code: 'default.description.label', default: 'Description')}" />
			--%>
            <th>
                <warehouse:message code="requisition.isPublished.label"/>
            </th>
            <th>
                <warehouse:message code="default.name.label"/>
            </th>

			<g:sortableColumn property="type"
				title="${warehouse.message(code: 'default.type.label', default: 'Type')}" />
            <g:sortableColumn property="type"
                title="${warehouse.message(code: 'commodityClass.label', default: 'Commodity Class')}" />
            <th>
                <warehouse:message code="requisition.origin.label"/>
            </th>
            <th>
                <warehouse:message code="requisition.destination.label"/>
            </th>
			<th>
				<warehouse:message code="requisition.requisitionItems.label"/>
			</th>
			<g:sortableColumn property="requestedBy"
				title="${warehouse.message(code: 'requisition.requestedBy.label', default: 'Requested by')}" />

            <g:sortableColumn property="createdBy"
                              title="${warehouse.message(code: 'default.createdBy.label', default: 'Created by')}" />

            <g:sortableColumn property="updateBy"
                              title="${warehouse.message(code: 'default.updatedBy.label', default: 'Updated by')}" />

            <g:sortableColumn property="dateCreated"
                              title="${warehouse.message(code: 'default.dateCreated.label', default: 'Date created')}" />

            <g:sortableColumn property="lastUpdated"
				title="${warehouse.message(code: 'default.lastUpdated.label', default: 'Last updated')}" />



        </tr>
	</thead>
	<tbody>
        <g:unless test="${requisitions}">
           	<tr class="prop odd">
           		<td colspan="12" class="center">
                    <div class="empty">
           			    <warehouse:message code="requisition.noRequisitionsMatchingCriteria.message"/>
                    </div>
	           	</td>
			</tr>     
		</g:unless>	
		<g:each in="${requisitions}" status="i" var="requisition">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td>			
					<g:render template="/requisitionTemplate/actions" model="[requisition:requisition]"/>
				</td>
                <%--
				<td>
					<label class="status"><format:metadata obj="${requisition?.status}"/></label>
				</td>
				<td>
					${requisition.requestNumber }
				</td>
				<td>
					<g:link action="show" id="${requisition.id}">						
						${fieldValue(bean: requisition, field: "name")}
					</g:link>
				</td>
				--%>
                <td>
                    <span class="${(requisition?.isPublished)?'active':'inactive'}">
                        <format:metadata obj="${requisition?.isPublished}"/>
                    </span>
                </td>
                <td>
                    <format:metadata obj="${requisition?.name}"/>
                </td>
				<td>
					<format:metadata obj="${requisition?.type}"/>
				</td>
                <td>
                    <format:metadata obj="${requisition?.commodityClass}"/>
                </td>
                <td>
                    <format:metadata obj="${requisition?.origin?.name}"/>
                </td>
                <td>
                    <format:metadata obj="${requisition?.destination?.name}"/>
                </td>
				<td>
                    <span class="count">${requisition?.requisitionItems?.size()?:0}</span>
				</td>
				<td>${requisition.requestedBy}</td>

                <td>${requisition.createdBy}</td>
                <td>${requisition.updatedBy}</td>
                <td><format:datetime obj="${requisition.dateCreated}" /></td>
                <td><format:datetime obj="${requisition.lastUpdated}" /></td>

			</tr>
		</g:each>
	</tbody>
</table>
