<table>
	<thead>
		<tr>
			<th>
				<warehouse:message code="default.actions.label"/>
			</th>
			<g:sortableColumn property="status" params="${pageParams}"
				title="${warehouse.message(code: 'default.status.label', default: 'Status')}" />

            <g:sortableColumn property="requestNumber" params="${pageParams}"
                              title="${warehouse.message(code: 'requisition.requestNumber.label', default: 'Request number')}" />

            <%--
            <g:sortableColumn property="type" params="${pageParams}"
                              title="${warehouse.message(code: 'default.type.label', default: 'Type')}" />

            <g:sortableColumn property="commodityClass" params="${pageParams}"
                              title="${warehouse.message(code: 'requisition.commodityClass.label', default: 'Commodity class')}" />
            --%>
			<g:sortableColumn property="description" params="${pageParams}"
				title="${warehouse.message(code: 'default.description.label', default: 'Description')}" />

            <g:sortableColumn property="origin" params="${pageParams}"
                              title="${warehouse.message(code: 'requisition.origin.label', default: 'Origin')}" />

            <th>
				<warehouse:message code="default.numItems.label"/>
			</th>
            <g:sortableColumn property="requestedBy" params="${pageParams}"
                              title="${warehouse.message(code: 'requisition.requestedBy.label', default: 'Requested by')}" />
            <th>
                <warehouse:message code="default.createdBy.label"/>
            </th>
            <th>
                <warehouse:message code="default.updatedBy.label"/>
            </th>

            <g:sortableColumn property="dateRequested" params="${pageParams}"
                              title="${warehouse.message(code: 'requisition.dateRequested.label', default: 'Date requested')}" />
            <%--

			<g:sortableColumn property="createdBy" params="${pageParams}"
				title="${warehouse.message(code: 'default.createdBy.label', default: 'Created by')}" />

            <g:sortableColumn property="updatedBy" params="${pageParams}"
                              title="${warehouse.message(code: 'default.updatedBy.label', default: 'Updated by')}" />

            <g:sortableColumn property="dateCreated" params="${pageParams}"
                              title="${warehouse.message(code: 'default.dateCreated.label', default: 'Date created')}" />

            <g:sortableColumn property="lastUpdated" params="${pageParams}"
                              title="${warehouse.message(code: 'default.lastUpdated.label', default: 'Last updated')}" />
            --%>

            <th></th>

		</tr>
	</thead>
	<tbody>
        <g:unless test="${requisitions}">
           	<tr class="prop odd">
           		<td colspan="9" class="center">
                    <div class="empty">
           			    <warehouse:message code="requisition.noRequisitionsMatchingCriteria.message"/>
                    </div>
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
                <td>
                    ${requisition?.origin?.name}

                </td>
				<td class="left bottom">
					<div class="count">${requisition?.requisitionItems?.size()?:0}</div>

                    <%--
                    ${warehouse.message(code: 'requisition.numRequisitionItems.label', args:[requisition?.requisitionItems?.size()?:0]) }
                    --%>
				</td>
				<td>${requisition.requestedBy?:warehouse.message(code:'default.none.label')}</td>
                <td>${requisition.createdBy?:warehouse.message(code:'default.none.label')}</td>
                <td>${requisition.updatedBy?:warehouse.message(code:'default.none.label')}</td>

                <td><format:date obj="${requisition.dateRequested}"/></td>

                <%--
                <td>${requisition.createdBy}</td>
                <td>${requisition.updatedBy}</td>
				<td><format:datetime obj="${requisition.dateCreated}" /></td>
                <td><format:datetime obj="${requisition.lastUpdated}" /></td>
                --%>
                <td>
                    <img src="${resource(dir:'images/icons/silk', file: 'zoom.png')}" class="dialog-trigger" data-id="#dialog-box-${requisition?.id}"/>
                    <div class="dialog-box" title="${requisition.requestNumber } ${requisition?.name}" id="dialog-box-${requisition?.id}">
                        <table>
                            <tr>
                                <th><warehouse:message code="default.quantity.label"/></th>
                                <th><warehouse:message code="product.productCode.label"/></th>
                                <th><warehouse:message code="product.label"/></th>
                                <th><warehouse:message code="requisitionItem.productPackage.label"/></th>
                            </tr>
                            <g:each var="requisitionItem" in="${requisition?.requisitionItems}" status="status">
                                <tr class="${status%2?'even':'odd'}">
                                    <td>
                                        ${requisitionItem?.product?.productCode}
                                    </td>
                                    <td>
                                        ${requisitionItem?.product}
                                    </td>
                                    <td>
                                        ${requisitionItem?.quantity}
                                    </td>
                                    <td>
                                        <g:if test="${requisitionItem?.productPackage}">
                                            ${requisitionItem?.productPackage?.uom?.code}/
                                            ${requisitionItem?.productPackage?.quantity}
                                        </g:if>
                                        <g:else>
                                            ${requisitionItem?.product?.unitOfMeasure}
                                        </g:else>
                                    </td>
                                </tr>
                            </g:each>
                        </table>
                    </div>

                </td>
			</tr>
		</g:each>
	</tbody>
</table>
